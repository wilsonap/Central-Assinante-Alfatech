package com.example.invoice

import com.example.data.local.InvoiceEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class InvoiceParserTest {

    @Test
    fun parseHarLikePayload_mapsBankAndOptionalBarcode() {
        val json = """
            {
              "abertas": [
                {
                  "id": 749428,
                  "id_cliente": 19,
                  "id_contrato": 12759,
                  "valor": "80.00",
                  "valor_aberto": "80.00",
                  "data_vencimento": "2026-08-17",
                  "status": "A",
                  "tipo_recebimento": "Boleto",
                  "linha_digitavel": "",
                  "status_text": "Em aberto"
                }
              ],
              "pagas": [],
              "canceladas": [],
              "vencidas": [],
              "pendentes": []
            }
        """.trimIndent()

        val result = InvoiceParser.parseGetFaturasJson(json, now = 1_700_000_000_000L)
        assertEquals(1, result.invoices.size)
        val inv = result.invoices.first()
        assertEquals("749428", inv.idReceber)
        assertEquals(8000L, inv.amountCents)
        assertEquals(8000L, inv.amountOpenCents)
        assertEquals("2026-08-17", inv.dueDate)
        assertEquals("A", inv.status)
        assertEquals(InvoiceEntity.BILLING_BANK, inv.billingType)
        assertEquals("Boleto", inv.rawBillingType)
        assertNull(inv.barcode)
        assertEquals(InvoiceEntity.GROUP_ABERTAS, inv.sourceGroup)
    }

    @Test
    fun nestedContractFaturaArray_andStoreType() {
        val json = """
            {
              "faturas": [
                {
                  "id_contrato": 1,
                  "fatura": [
                    {
                      "id": "100",
                      "valor": 129.90,
                      "data_vencimento": "17/08/2026",
                      "status": "A",
                      "tipo_recebimento": "Fatura"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()
        val result = InvoiceParser.parseGetFaturasJson(json)
        assertEquals(1, result.invoices.size)
        assertEquals("100", result.invoices[0].idReceber)
        assertEquals(12990L, result.invoices[0].amountCents)
        assertEquals("2026-08-17", result.invoices[0].dueDate)
        assertEquals(InvoiceEntity.BILLING_STORE, result.invoices[0].billingType)
    }

    @Test
    fun pixRawStaysUnknown_notPaid() {
        assertEquals(InvoiceEntity.BILLING_UNKNOWN, InvoiceBillingType.fromRaw("Pix"))
    }

    @Test
    fun invalidRowsSkipped() {
        val json = """{"abertas":[{"id":1,"status":"A"},{"valor":"10.00","status":"A"}]}"""
        val result = InvoiceParser.parseGetFaturasJson(json)
        assertTrue(result.invoices.isEmpty())
        assertTrue(result.skipped >= 1)
    }
}
