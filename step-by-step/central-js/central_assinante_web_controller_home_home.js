buscarHomeHTML();

var funcoes_home = {

    desbloqueioConfianca(id_contrato, valida_loading) {
        var hs_web = new HotsiteWeb();
        hs_web.desbloqueioConfianca(id_contrato, valida_loading);
    },
    liberacaoSuspensaoParcial: function (id_contrato, valida_loading) {
        var hs_web = new HotsiteWeb();
        hs_web.liberacaoSuspensaoParcial(id_contrato, valida_loading);
    },
    validarMensagemLida(id_atendimento) {
        var hs_web = new HotsiteWeb();
        hs_web.validarMensagemLida(id_atendimento);
    },
    enviarFaturaSMS(tipo, id_receber) {
        var hs_web = new HotsiteWeb();
        hs_web.enviarFaturaSMS(tipo, id_receber);
    },
    enviarFaturaEMAIL(id_receber) {
        var hs_web = new HotsiteWeb();
        hs_web.enviarFaturaEMAIL(id_receber);
    },
    imprimirFatura(id_receber,alt_gateway = false) {
        app.modal_url = '';
        app.modal_nome = 'Fatura - ' + id_receber;
        var hs_web = new HotsiteWeb();
        hs_web.imprimirFatura(id_receber,undefined,alt_gateway);
    },
    imprimirPix(id_receber) {
        app.modal_url = '';
        var hs_web = new HotsiteWeb();
        hs_web.imprimirPix(id_receber);
    },
    imprimirPlano(id_plano, id_termo = 0, cliente_contrato_assinatura_termo_id = 0) {
        app.modal_url = '';
        app.modal_nome = 'Plano - ' + id_plano;
        var hs_web = new HotsiteWeb();
        hs_web.imprimirPlano(id_plano, id_termo, cliente_contrato_assinatura_termo_id);
    },
    assinarContratoDigital(link) {
        if (link.length > 0){
            window.open(link, '_blank');
        }
    },
    carregarConsumo(index, consumos_download, consumos_upload, labels, x_label) {
        var hs_web = new HotsiteWeb();
        hs_web.graficoConsumo(index, consumos_download, consumos_upload, labels, x_label);
    },
    montarModalConsultaFatura(fatura) {
        var hs_web = new HotsiteWeb();
        hs_web.montarModalConsultaFatura(fatura);
    },
    imprimirFaturaComSelecaoDeBanco(dadosReceber,dadosCarteira){
        let hs_web = new HotsiteWeb();
        hs_web.imprimirFaturaComSelecaoDeBanco(dadosReceber,dadosCarteira);
    },
    montarModalSelecionarBanco(fatura,tamanho) {
        var hs_web = new HotsiteWeb();
        hs_web.montarModalSelecionarBanco(fatura,tamanho);
    },
    copiarCodigoBarras(linha_digitavel) {
        linha_digitavel = linha_digitavel.replaceAll(".","").replaceAll(" ","");

        if(window.ReactNativeWebView){
            return postMessageMobile("copy_bar_code", JSON.stringify({dados: linha_digitavel}))
                .then((retorno)=>funcoes_faturas.validateReturnCopyBarCode(JSON.parse(retorno.data).retorno))
                .catch(()=>funcoes_faturas.validateReturnCopyBarCode(false));
        }

        navigator.clipboard.writeText(linha_digitavel)
            .then(()=>funcoes_faturas.validateReturnCopyBarCode(true))
            .catch(()=>funcoes_faturas.validateReturnCopyBarCode(false));
    },
    validateReturnCopyBarCode(successful){
        const hs_web = new HotsiteWeb();
        if (successful) {
            hs_web.criarToast(4000, 'Sucesso!', 'Código copiado para área de transferência.', 'fas fa-check', 'green', 'id_sucesso');
        } else {
            hs_web.criarToast(4000, 'Erro!', 'Não foi possível copiar o código.', 'fas fa-exclamation-circle', 'red', 'id_erro');
        }
    },
    montarModalEntreEmContato() {
        let hs_web = new HotsiteWeb();
        hs_web.montarModalEntreEmContato();
    },
};

function linkTabela(id_atendimento) {
    funcoes_home.validarMensagemLida(id_atendimento);
    router.replace({path: '/central_assinante_web/atendimentos/mensagens/' + id_atendimento});
}

function buscarHomeHTML() {
    try {
        $.ajax({
            url: __SERVER__ + "/view/home/home.vue",
            type: "GET",
            data: "",
            dataType: 'html'
        }).done(function (resposta) {
            __ROTAS__.push({
                path: '/central_assinante_web/',
                name: 'home',
                component: {
                    template: resposta,
                    data() {
                        return {
                            faturas: {
                                vencidas: [],
                                pendentes: [],
                                abertas: [],
                                pagas: [],
                                canceladas: [],
                                notifi_dash: [],
                                num_notificacoes: 0,
                                total_apagar_dash: 0.00,
                                num_faturas: 0,
                                total_apagar_faturas: 0.00,
                            },
                            atendimentos: {
                                atendimentos: [],
                                atendimentos_abertos: [],
                                atendimentos_finalizados: [],
                                atendimentos_dash: [],
                                num_atendimentos: 0
                            },
                            planos: {
                                pre_contratos: [],
                                financeiro_atrasado: [],
                                ativo_bloqueado: [],
                                ativo_bloqueado_desbloqueio_n: [],
                                ativo_desbloqueado: [],
                                ativo: [],
                                outros_status: [],
                                contratos_dash: [],
                                num_contratos_dash: 0
                            },
                            consumos: {
                                consumo_diario: '',
                                consumo_mensal: '',
                                consumo_ultima_semana: '',
                                consumo_ultimo_mes: ''
                            },
                            franquias: [],
                            maximizado: true,
                            loading: true,
                            assinatura_termo: false,
                            id_termo: 0,
                            cliente_contrato_assinatura_termo_id: 0,
                            id_contrato_assinatura: 0,
                            url_fatura: '',
                            url_plano: '',
                            mostrar_franquia: 'N',
                            mostrar_fatura: 'N',
                            mostrar_plano: 'N',
                            mostrar_atendimento: 'N',
                            sms_fatura: 'N',
                            email_fatura: 'N',
                            imprime_venda_fatura: 'N',
                            ver_franquias: false,
                            tamanho: $(window).width(),
                            is_safari: false,
                            ja_enviou_requisicao: false,
                        };
                    },
                    mounted: function () {
                        $("#assinarContratoHome").on("shown.bs.modal", function () {
                            app.assinando = true;

                            var wrapper = document.getElementById("signature-pad"),
                                closeButton = document.querySelector("#assinarContratoHome button[data-dismiss=modal]"),
                                clearButton = wrapper.querySelector("#assinarContratoHome button[data-action=clear]"),
                                saveButton = wrapper.querySelector("#assinarContratoHome button[data-action=save]"),
                                canvas = wrapper.querySelector("canvas"),
                                signaturePad;

                            canvas.width = $("#signature-pad-body").width();
                            canvas.height = 150;

                            signaturePad = new SignaturePad(canvas, {
                                minWidth: 0.1,
                                maxWidth: 1.5,
                                penColor: "rgb(0, 84, 150)"
                            });

                            clearButton.addEventListener("click", function (event) {
                                signaturePad.clear();
                            });

                            $(window).resize(() => {
                                const signaturePadTmp = signaturePad.toData();
                                canvas.width = $("#signature-pad-body").width();
                                signaturePad.clear();
                                signaturePad.fromData(signaturePadTmp);
                            });

                            if (!router.history.current.matched[0].instances.default.ja_enviou_requisicao) {
                                saveButton.addEventListener("click", function (event) {
                                    $(saveButton).attr("disabled", true);
                                    if (signaturePad.isEmpty()) {
                                        $(saveButton).attr("disabled", false);
                                        closeButton.click();
                                        var hs_web = new HotsiteWeb();
                                        hs_web.criarToast('2000', 'Erro!', 'Campo assinatura está em branco.', 'fas fa-exclamation-circle', 'red', 'id_erro');
                                    } else {
                                        $(saveButton).attr("disabled", false);
                                        id_contrato_assinatura = router.history.current.matched[0].instances.default.id_contrato_assinatura;
                                        assinatura_termo = router.history.current.matched[0].instances.default.assinatura_termo;
                                        assinatura = signaturePad.toDataURL();
                                        signaturePad.clear();
                                        closeButton.click();
                                        var hs_web = new HotsiteWeb();
                                        if (assinatura_termo) {
                                            id_termo = router.history.current.matched[0].instances.default.id_termo;
                                            cliente_contrato_assinatura_termo_id = router.history.current.matched[0].instances.default.cliente_contrato_assinatura_termo_id;

                                            hs_web.salvarAssinaturaTermo(id_contrato_assinatura, assinatura, true, id_termo, cliente_contrato_assinatura_termo_id);
                                        } else {
                                            hs_web.salvarAssinatura(id_contrato_assinatura, assinatura, true);
                                        }
                                    }
                                });
                            }
                            router.history.current.matched[0].instances.default.ja_enviou_requisicao = true;

                        });
                        $('#assinarContratoHome').on('hidden.bs.modal', function () {
                            app.assinando = false;
                        });
                        $(document).ready(function () {
                            validarFundoLogin();
                        });
                    },
                    methods: funcoes_home
                }
            });

            PAGE_HOME = true;
            verificaPages();
        });
    } catch (e) {
        console.log("Error Message -> " + e);
    }
}
