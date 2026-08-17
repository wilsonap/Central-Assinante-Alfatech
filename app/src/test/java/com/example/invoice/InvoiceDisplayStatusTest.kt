package com.example.invoice

import com.example.data.local.InvoiceEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InvoiceDisplayStatusTest {

    private val today = "2026-08-17"

    private fun invoice(
        due: String = "2026-08-22",
        status: String = "A",
        statusText: String? = "Em aberto",
        group: String? = InvoiceEntity.GROUP_ABERTAS
    ) = InvoiceEntity(
        idReceber = "1",
        amountCents = 8000,
        dueDate = due,
        status = status,
        statusText = statusText,
        billingType = InvoiceEntity.BILLING_BANK,
        sourceGroup = group
    )

    @Test
    fun paga_porStatusP() {
        val s = InvoiceDisplayStatusMapper.getInvoiceDisplayStatus(
            invoice(status = "P", statusText = "Paga", group = InvoiceEntity.GROUP_PAGAS),
            today
        )
        assertEquals("Paga", s.label)
        assertEquals(InvoiceVisualKind.PAID, s.kind)
    }

    @Test
    fun paga_porStatusR_recebida() {
        val s = InvoiceDisplayStatusMapper.getInvoiceDisplayStatus(
            invoice(status = "R", statusText = "Recebido", group = null),
            today
        )
        assertEquals("Paga", s.label)
        assertEquals(InvoiceVisualKind.PAID, s.kind)
    }

    @Test
    fun venceEm5Dias() {
        val s = InvoiceDisplayStatusMapper.getInvoiceDisplayStatus(
            invoice(due = "2026-08-22"),
            today
        )
        assertEquals("Vence em 5 dias", s.label)
        assertEquals(InvoiceVisualKind.OPEN_FUTURE, s.kind)
        assertEquals(5, s.days)
    }

    @Test
    fun venceAmanha() {
        val s = InvoiceDisplayStatusMapper.getInvoiceDisplayStatus(
            invoice(due = "2026-08-18"),
            today
        )
        assertEquals("Vence amanhã", s.label)
        assertEquals(InvoiceVisualKind.DUE_TOMORROW, s.kind)
    }

    @Test
    fun venceHoje() {
        val s = InvoiceDisplayStatusMapper.getInvoiceDisplayStatus(
            invoice(due = "2026-08-17"),
            today
        )
        assertEquals("Vence hoje", s.label)
        assertEquals(InvoiceVisualKind.DUE_TODAY, s.kind)
    }

    @Test
    fun vencidaHa1Dia() {
        val s = InvoiceDisplayStatusMapper.getInvoiceDisplayStatus(
            invoice(due = "2026-08-16", group = InvoiceEntity.GROUP_VENCIDAS),
            today
        )
        assertEquals("Vencida há 1 dia", s.label)
        assertEquals(InvoiceVisualKind.OVERDUE, s.kind)
        assertEquals(1, s.days)
    }

    @Test
    fun vencidaHa3Dias() {
        val s = InvoiceDisplayStatusMapper.getInvoiceDisplayStatus(
            invoice(due = "2026-08-14", group = InvoiceEntity.GROUP_VENCIDAS),
            today
        )
        assertEquals("Vencida há 3 dias", s.label)
        assertEquals(InvoiceVisualKind.OVERDUE, s.kind)
        assertEquals(3, s.days)
    }

    @Test
    fun dataInvalida_fallback() {
        val s = InvoiceDisplayStatusMapper.getInvoiceDisplayStatus(
            invoice(due = "invalid", statusText = "Em aberto"),
            today
        )
        assertEquals("Em aberto", s.label)
        assertEquals(InvoiceVisualKind.FALLBACK, s.kind)
        assertNull(s.days)
    }

    @Test
    fun paga_naoUsaRegraDeVencimento() {
        val s = InvoiceDisplayStatusMapper.getInvoiceDisplayStatus(
            invoice(
                due = "2026-08-14",
                status = "P",
                statusText = "Paga",
                group = InvoiceEntity.GROUP_PAGAS
            ),
            today
        )
        assertEquals("Paga", s.label)
        assertEquals(InvoiceVisualKind.PAID, s.kind)
    }

    @Test
    fun cancelada() {
        val s = InvoiceDisplayStatusMapper.getInvoiceDisplayStatus(
            invoice(status = "C", statusText = "Cancelada", group = InvoiceEntity.GROUP_CANCELADAS),
            today
        )
        assertEquals("Cancelada", s.label)
        assertEquals(InvoiceVisualKind.CANCELLED, s.kind)
    }
}
