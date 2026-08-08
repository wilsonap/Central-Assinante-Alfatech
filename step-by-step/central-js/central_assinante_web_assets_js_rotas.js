function verificaPages() {
    if (PAGE_HOME && PAGE_DADOS_CLIENTE && PAGE_ATENDIMENTOS && PAGE_MENSAGENS_ATENDIMENTO && PAGE_NOVO_ATENDIMENTO && PAGE_FATURAS && PAGE_PLANOS && PAGE_NOTAS && PAGE_ERRO && PAGE_CONSUMOS && PAGE_PAGAMENTOS && PAGE_RELATORIOS && PAGE_CONFIG && PAGE_SPEEDTEST && PAGE_CADASTRO_LOGIN && PAGE_CONNECTIONS && PAGE_RECORRENCIA_VINDI) {
        setarRotas(__ROTAS__);
    }
}

function setarRotas(rotas) {
    Vue.use(vueDirectiveTooltip);
    const routes = rotas;
    router = new VueRouter({
        routes,
        mode: 'history',
    });

    const data = new Date();
    const dia = data.getDate().toString();
    const diaF = (dia.length == 1) ? `0${dia}` : dia;
    const mes = (data.getMonth() + 1).toString(); // +1 pois no getMonth Janeiro começa com zero.
    const mesF = (mes.length == 1) ? `0${mes}` : mes;
    const anoF = data.getFullYear();
    dataFormatada = `${diaF}/${mesF}/${anoF}`;

    app = new Vue({
        data: {
            pg_fatura: 'N',
            pg_plano: 'N',
            pg_nota: 'N',
            pg_consumo: 'N',
            pg_atendimento: 'N',
            pg_declaracao_debito: 'N',
            pg_alterar_senha: 'N',
            pg_config: 'N',
            pg_connections: 'N',
            sms_fatura: 'N',
            email_fatura: 'N',
            imprime_venda_fatura: 'N',
            cad_cli_fantasia: 'E',
            cad_cli_cpf: 'E',
            cad_cli_rg: 'E',
            cad_cli_data_nascimento: 'E',
            cad_cli_telefone: 'E',
            cad_cli_celular: 'E',
            cad_cli_telefone_comercial: 'E',
            cad_cli_ramal: 'E',
            cad_cli_cep: 'E',
            cad_cli_endereco: 'E',
            cad_cli_numero: 'E',
            cad_cli_bairro: 'E',
            cad_cli_complemento: 'E',
            cad_cli_referencia: 'E',
            cad_cli_cidade: 'E',
            cad_cli_sexo: 'E',
            tipo_login: 'E',
            mostra_builder: false,
            cliente_nome: '',
            maximizado: true,
            loading: true,
            loading_modal: true,
            barra_loading: false,
            login: false,
            modal_url: '',
            modal_nome: '',
            logo_base: '',
            img_logo_base: '',
            cordova_app: false,
            assinando: false,
            url_app_android: '',
            url_generate_app: 'https://builder.ixcsoft.com.br/',
            permitir_cadastros_usuario: 'N',
            pdf_fatura_nota: '',
            link_fatura: '',
            consultar_fatura_modal: { dados_logo_banco: [] },
            carteiras_faturas: [],
            tamanho_tela: 0,
            whatsapp_link: '',
            webchat_link: '',
            telefone_contato: '',
            habilitar_contato: 'N',
            suspensao_data_inicial: dataFormatada,
            suspensao_data_final: '',
            whatsapp_link: '',
            webchat_link: '',
            telefone_contato: '',
            pg_partiu_desconto: 'N',
            pg_partiu_cnpj_cpf: '',
            pg_partiu_id_integracao: '',
            pg_partiu_desconto_link: '',
            is_fn: false,
            showSpeedTest: false,
            device_config_modal: false,
            data_wifi: {
                type: false,
                ssid: '',
                passwordWifi1: '',
                passwordWifi2: '',
            },
            inputType: 'password',
            inputType2: 'password',
            sva_permission: '',
            sva_link: '',
            svaPlatform: '',
            svaUsername: '',
            svaExternalId: '',
            svaIntegrationId: '',
        },
        methods: {
            encerrarSessao() {
                app.loading = true;
                const hs_web = new HotsiteWeb();
                hs_web.encerrarSessaoHotsite();
            },
            resizedataURL(datas, wantedWidth = 120, wantedHeight = 55) {
                return datas;
            },
            getDadosServidor() {
                new HotsiteWeb().getDadosServidor();
            },
        },
        computed: {
            classDisableButtonSalvar() {
                return {
                    'button-devices-disable':
                        !this.data_wifi.ssid
                        || this.data_wifi.passwordWifi1 != this.data_wifi.passwordWifi2
                        || !this.data_wifi.passwordWifi1
                        || !this.data_wifi.passwordWifi2
                        || this.data_wifi.passwordWifi1.length < 8
                        || this.data_wifi.passwordWifi2.length < 8,
                };
            },
        },
        router,
    }).$mount('#app');

    ROTA = window.location.pathname;

    validaAcessoHash();

    validaRotas();
    const url = window.location.pathname;
    condicoesRotas(url);
}

function validaRotas() {
    router.afterEach((rota) => {
        condicoesRotas(rota);
    });
}

function condicoesRotas(rota) {
    const hs_web = new HotsiteWeb();
    hs_web.setFavIcon();
    if (CAD_LOGIN) {
        if (hs_web.validaSessao()) {
            sessionStorage.removeItem('reload');
            hs_web.validaExisteDados();
            app.loading = true;
            if (rota.path) {
                ROTA = rota.path;
                if (rota.path == '/central_assinante_web/') {
                    LOADING_FATURA = true;
                    LOADING_FRANQUIA = true;
                    LOADING_PLANO = true;
                    LOADING_ATENDIMENTO = true;
                    hs_web.buscarFranquias(2000000000, true);
                    hs_web.buscarFaturas(2000000000, true, '', true);
                    hs_web.buscarCarteirasFatura();
                    hs_web.buscarPlanos(2000000000, true, '', '', true);
                    hs_web.buscarAtendimentos(2000000000, true, '', '', '', true);
                    hs_web.buscarConsumos(true);
                    hs_web.partiuClubeDeVantagens();
                    hs_web.callSvaIntegration();
                } else if (rota.path == '/central_assinante_web/dados_cliente') {
                    hs_web.setarPermissaoDadosCliente();
                    hs_web.buscarDadosCliente();
                } else if (rota.path == '/central_assinante_web/atendimentos' && app.pg_atendimento == 'S') {
                    SLICE_ATENDIMENTO = 5;
                    hs_web.buscarAtendimentos(SLICE_ATENDIMENTO, false, [], '', true);
                } else if (rota.path == '/central_assinante_web/atendimentos/novo_atendimento' && app.pg_atendimento == 'S') {
                    hs_web.buscarProtocolo();
                    hs_web.buscarDepartamento();
                    hs_web.buscarAssunto();
                } else if (rota.path == `/central_assinante_web/atendimentos/mensagens/${rota.params.protocolo}` && app.pg_atendimento == 'S') {
                    hs_web.buscarMensagensAtendimento(rota.params.protocolo);
                } else if (rota.path == '/central_assinante_web/faturas' && app.pg_fatura == 'S') {
                    SLICE_FATURA = 5;
                    hs_web.buscarFaturas(SLICE_FATURA, false, true);
                    hs_web.buscarCarteirasFatura();
                } else if (rota.path == `/central_assinante_web/faturas/pagamentos/${rota.params.protocolo}` && app.pg_fatura == 'S') {
                    hs_web.buscarParcelas(rota.params.protocolo);
                    app.loading = false;
                } else if (rota.path == '/central_assinante_web/planos' && app.pg_plano == 'S') {
                    SLICE_PLANO = 5;
                    hs_web.buscarPlanos(SLICE_PLANO, false, '', true);
                } else if (rota.path == '/central_assinante_web/notas' && app.pg_nota == 'S') {
                    SLICE_NOTA = 5;
                    hs_web.buscarNotas(SLICE_NOTA);
                } else if (rota.path == '/central_assinante_web/consumos' && app.pg_consumo == 'S') {
                    hs_web.buscarConsumos();
                } else if (rota.path == '/central_assinante_web/relatorios') {
                    hs_web.validaAcessoRelatorios();
                    hs_web.getAnosQuitacao();
                } else if (rota.path == '/central_assinante_web/configuracoes') {
                    $(document).ready(() => {
                        hs_web.getContratosRecorrentes();
                        app.loading = false;
                    });
                } else if (rota.path == '/index.php') {
                    router.replace('/central_assinante_web/');
                    hs_web.getDadosServidor();
                } else if (rota.path == '/central_assinante_web/speed_test') {
                    hs_web.getDadosServidor();
                } else if (rota.path == '/central_assinante_web/connections') {
                    hs_web.getConnectionsAcs();
                } else if (rota.path == `/central_assinante_web/faturas/recorrencia_vindi/${rota.params.id_contrato}`) {
                    hs_web.recorrenciaVindi();
                    app.loading = false;
                } else {
                    router.replace('/central_assinante_web/');
                    hs_web.getDadosServidor();
                    app.loading = false;
                }
            }

            if (typeof rota === 'string') {
                ROTA = rota;
                const protocolo = validaProtocolo(rota);
                if (rota == '/central_assinante_web/') {
                    hs_web.setarPermissaoHome();
                    LOADING_FATURA = true;
                    LOADING_FRANQUIA = true;
                    LOADING_PLANO = true;
                    LOADING_ATENDIMENTO = true;
                    hs_web.buscarFranquias(2000000000, true);
                    hs_web.buscarFaturas(2000000000, true, '', true);
                    hs_web.buscarCarteirasFatura();
                    hs_web.partiuClubeDeVantagens();
                    hs_web.callSvaIntegration();

                    hs_web.buscarPlanos(2000000000, true, '', '', true);
                    hs_web.buscarAtendimentos(2000000000, true, '', '', '', true);
                    hs_web.buscarConsumos(true);
                } else if (rota == '/central_assinante_web/dados_cliente' || rota == '/central_assinante_web/dados_cliente/') {
                    hs_web.setarPermissaoDadosCliente();
                    hs_web.buscarDadosCliente();
                } else if ((rota == '/central_assinante_web/atendimentos' || rota == '/central_assinante_web/atendimentos/') && app.pg_atendimento == 'S') {
                    SLICE_ATENDIMENTO = 5;
                    hs_web.buscarAtendimentos(SLICE_ATENDIMENTO, false, [], '', true);
                } else if ((rota == '/central_assinante_web/atendimentos/novo_atendimento' || rota == '/central_assinante_web/atendimentos/novo_atendimento/') && app.pg_atendimento == 'S') {
                    hs_web.buscarProtocolo();
                    hs_web.buscarDepartamento();
                    hs_web.buscarAssunto();
                } else if ((rota == `/central_assinante_web/atendimentos/mensagens/${protocolo}` || rota == `/central_assinante_web/atendimentos/mensagens/${protocolo}/`) && app.pg_atendimento == 'S') {
                    hs_web.buscarMensagensAtendimento(protocolo);
                    $(document).ready(() => {
                        ativaSwipe();
                    });
                } else if ((rota == '/central_assinante_web/faturas' || rota == '/central_assinante_web/faturas/') && app.pg_fatura == 'S') {
                    hs_web.setarPermissaoFaturas();
                    hs_web.buscarFaturas(SLICE_FATURA, false, true);
                    hs_web.buscarCarteirasFatura();
                } else if ((rota == `/central_assinante_web/faturas/pagamentos/${protocolo}` || rota == `/central_assinante_web/faturas/pagamentos/${protocolo}/`) && app.pg_fatura == 'S') {
                    hs_web.buscarParcelas(protocolo);
                    app.loading = false;
                } else if ((rota == '/central_assinante_web/planos' || rota == '/central_assinante_web/planos/') && app.pg_plano == 'S') {
                    SLICE_PLANO = 5;
                    hs_web.buscarPlanos(SLICE_PLANO, false, '', true);
                } else if ((rota == '/central_assinante_web/notas' || rota == '/central_assinante_web/notas/') && app.pg_nota == 'S') {
                    SLICE_NOTA = 5;
                    hs_web.buscarNotas(SLICE_NOTA);
                } else if ((rota == '/central_assinante_web/consumos' || rota == '/central_assinante_web/consumos/') && app.pg_consumo == 'S') {
                    hs_web.buscarConsumos();
                } else if ((rota == '/central_assinante_web/relatorios' || rota == '/central_assinante_web/relatorios/')) {
                    hs_web.validaAcessoRelatorios();
                    hs_web.getAnosQuitacao();
                } else if (rota == '/central_assinante_web/configuracoes' || rota == '/central_assinante_web/configuracoes/') {
                    $(document).ready(() => {
                        hs_web.getContratosRecorrentes();
                        app.loading = false;
                    });
                } else if (rota == '/index.php') {
                    router.replace('/central_assinante_web/');
                } else if (rota == '/central_assinante_web/speed_test' || rota == '/central_assinante_web/speed_test/') {
                    $(document).ready(() => {
                        hs_web.getDadosServidor();
                    });
                } else if (rota == '/central_assinante_web/connections' || rota == '/central_assinante_web/connections/') {
                    $(document).ready(() => {
                        hs_web.getConnectionsAcs();
                    });
                } else if (rota == `/central_assinante_web/faturas/recorrencia_vindi/${protocolo}` || rota == `/central_assinante_web/faturas/recorrencia_vindi/${protocolo}/`) {
                    hs_web.recorrenciaVindi();
                    app.loading = false;
                } else {
                    router.replace('/central_assinante_web/');
                    app.loading = false;
                }
            }

            $(document).ready(() => {
                isFirefox();
                isSafari();
                $('.topbar').css('display', 'flex');
                $('.sidebar').css('display', 'block');
                ativaSwipe();
                iniciaFirebase();
            });
        } else {
            const isAndroid = /android/i.test(navigator.userAgent);
            if (isAndroid && typeof window.ReactNativeWebView === 'undefined' && !window.hasValidatedLinking) {
                new HotsiteWeb().getAppURL();
            }

            if (rota === '/central_assinante_web/trocarSenha' || location.href.includes('central_assinante_web/?hash=')) {
                const hash = (new URL(window.location.href)).searchParams.get('hash');
                localStorage.setItem('trocarSenhaHash', JSON.stringify(hash));
                if (hash) {
                    router.replace(`/central_assinante_web/trocarSenha?hash=${hash}`);
                } else {
                    router.replace('/central_assinante_web/login');
                }
            } else {
                const reload = sessionStorage.getItem('reload');
                if (!reload) {
                    sessionStorage.setItem('reload', true);
                    if ((window.location.port == '') || (typeof window.location.port === undefined)) {
                        window.location.href = __SERVER__;
                    } else if (!isNaN(parseInt(window.location.port))) {
                        window.location.reload();
                    }
                }

                window.hasValidatedLinking = true;
                router.replace('/central_assinante_web/login');
                hs_web.buscarTipoLogin();
            }
        }
        app.oner = __PARAMETROS__[0]?.ONER ?? '';
    } else {
        hs_web.setarPermissaoDadosCliente();
        hs_web.buscarDadosCliente();
        router.replace('/central_assinante_web/cadastro_login');
    }
}

function verificarAppEExecutarOuRedirecionar(mensagem) {
    const appScheme = 'centralassinanteappixc://login';
    const appUrlAndroid = mensagem;

    if (appUrlAndroid) {
        const confirmacao = confirm('Você quer abrir o aplicativo?');
        if (confirmacao) {
            try {
                setTimeout(() => {
                    window.location.href = appScheme;
                }, 5000);
                window.location.href = appUrlAndroid;
            } catch (error) {
                window.location.href = appUrlAndroid;
            }
        }
    }
}

function setDataFinalSuspensao() {
    app.suspensao_data_final = $('#inputDataFinalSuspensao')[0].value;
}

function validaProtocolo(rota) {
    let protocolo = rota.split('/');
    if ((protocolo[2] == 'atendimentos' && protocolo[3] == 'mensagens') || (protocolo[2] == 'faturas' && protocolo[3] == 'pagamentos') || (protocolo[2] == 'faturas' && protocolo[3] == 'recorrencia_vindi')) {
        if (protocolo[protocolo.length - 1] && protocolo[protocolo.length - 1] != 'mensagens' && protocolo[protocolo.length - 1] != 'pagamentos' && protocolo[protocolo.length - 1] != '') {
            protocolo = protocolo[protocolo.length - 1];
        } else if (protocolo[protocolo.length - 2] && protocolo[protocolo.length - 2] != 'mensagens' && protocolo[protocolo.length - 2] != 'pagamentos' && protocolo[protocolo.length - 2] != '') {
            protocolo = protocolo[protocolo.length - 2];
        }
    }

    return protocolo;
}

function validaAcessoHash() {
    const hs_web = new HotsiteWeb();
    const sessao = localStorage.getItem('sessao') ? localStorage.getItem('sessao') : sessionStorage.getItem('sessao');
    const dados_cliente = localStorage.getItem('dados') ? localStorage.getItem('dados') : sessionStorage.getItem('dados');
    const parametros = localStorage.getItem('parametros') ? localStorage.getItem('parametros') : sessionStorage.getItem('parametros');

    if (!sessao && !dados_cliente && !parametros) {
        app.loading = true;
        return hs_web.validaFNProject();
    }

    CAD_LOGIN = true;
}

function chamarFuncaoHotsiteWeb(nomeFuncao, paramFuncao) {
    const hs_web = new HotsiteWeb();

    switch (nomeFuncao) {
    case 'copiarCodigoBarrasModal':
        hs_web.copiarCodigoBarrasModal(paramFuncao.linha_digitavel);
        break;
    case 'enviarFaturaSMS':
        hs_web.enviarFaturaSMS(paramFuncao.tipo, paramFuncao.id_receber);
        break;
    case 'enviarFaturaEMAIL':
        hs_web.enviarFaturaEMAIL(paramFuncao.id_receber);
        break;
    case 'imprimirFatura':
        hs_web.imprimirFatura(paramFuncao.id_receber, undefined, paramFuncao.alt_gateway);
        break;
    case 'imprimirPix':
        hs_web.imprimirPix(paramFuncao.id_receber);
        break;
    case 'imprimirFaturaComSelecaoDeBanco':
        hs_web.imprimirFaturaComSelecaoDeBanco(paramFuncao.dados_receber, paramFuncao.dados_carteira);
        break;
    case 'montarModalConsultaFatura':
        hs_web.montarModalConsultaFatura(paramFuncao.dados_receber);
        break;
    case 'montarModalSelecionarBanco':
        hs_web.montarModalSelecionarBanco(paramFuncao.dados_receber);
        break;
    case 'copiarTelefoneContato':
        hs_web.copiarTelefoneContato(paramFuncao.telefone_contato);
        break;
    case 'suspenderContrato':
        hs_web.suspenderContrato(paramFuncao.id_contrato, paramFuncao.data_inicial_suspensao, paramFuncao.data_final_suspensao);
        break;
    case 'removerSuspensaoContrato':
        hs_web.removerSuspensaoContrato(paramFuncao.id_contrato);
        break;
    case 'changedConfigWifi':
        hs_web.changedConfigWifi(paramFuncao);
        break;
    case 'generateLinkSva':
            hs_web.generateLinkSva(paramFuncao);
            break;
    default:
        break;
    }
}

function invoiceAllowsPaymentByPix(invoice) {
    const gateways =
        [
            'galaxPay',
            'fortunus',
            'bb_api',
            'sicoobApi',
            'iugu',
            'sicrediApi',
            'widepay',
            'santander',
            'lytex',
            'asaas',
            'btg',
            'bradesco'
        ];

    return invoice.habilitar_pix === 'S' && (
        gateways.includes(invoice.fn_carteira_cobranca_gateway_nome) || (
            invoice.id_wallet_pix > 0
            || invoice.tipo_recebimento === 'Pix'
            || invoice.central_pix > 0
        )
    );
}

const promisesWebView = [];

function postMessageMobile(type, dados) {
    const id = `_${Math.random().toString(36).substr(2, 9)}`;

    promisesWebView[id] = {};
    promisesWebView[id].promise = new Promise((resolve, reject) => {
        promisesWebView[id].resolve = resolve;
        promisesWebView[id].reject = reject;
        window.ReactNativeWebView.postMessage(JSON.stringify({ type, promiseID: id, dados }));
    });

    return promisesWebView[id].promise;
}

if (typeof window.ReactNativeWebView !== 'undefined') {
    addEventListener('message', (event) => {
        const data = JSON.parse(event.data);
        if (data.type === 'getUrl') {
            return postMessageMobile('returnurl', JSON.stringify({ currentUrl: window.location.href, homeUrl: data.home }));
        }
        return promisesWebView[data.promiseID].resolve(event);
    }, true);
}

function adicionarDias(days) {
    const dataInicial = new Date();
    dataInicial.setDate(dataInicial.getDate() + parseInt(days));
    const dataFormatada = `${dataInicial.getDate()}/${(dataInicial.getMonth()) + 1}/${dataInicial.getFullYear()}`;
    return dataFormatada;
}

function fecharModalSuspensao(intervalo_entre_suspensao, prolonga_fidelidade_tempo_suspensao, tempo_min_suspensao) {
    const closeButton = document.querySelector('#modalSuspensao button[data-dismiss=modal]');
    const intervaloEntreSuspensao = document.querySelector('#intervaloEntreSuspensao');
    const prolongaFidelidadeTempoSuspensao = document.querySelector('#prolongaFidelidadeTempoSuspensao');
    const tempoMinSuspensao = document.querySelector('#tempoMinSuspensao');

    if (prolonga_fidelidade_tempo_suspensao === 'S') {
        prolongaFidelidadeTempoSuspensao.innerHTML = 'Sua fidelidade ser&#225 aumentada de acordo com a quantidade de dias que o contrato estiver suspenso.';
    }

    intervaloEntreSuspensao.innerHTML = intervalo_entre_suspensao;
    tempoMinSuspensao.innerHTML = adicionarDias(tempo_min_suspensao);
    closeButton.click();
}

function fecharModalRemoverSuspensao(data_inicial_suspensao, data_final_suspensao, total_dias_suspensao) {
    const closeButton = document.querySelector('#modalRemoveSuspensao button[data-dismiss=modal]');
    const totalDiasSuspensao = document.querySelector('#totalDiasSuspensao');

    app.suspensao_data_inicial = data_inicial_suspensao;
    app.suspensao_data_final = data_final_suspensao;
    totalDiasSuspensao.innerHTML = total_dias_suspensao;

    closeButton.click();
}

function organizarModalRemoverSuspensao(dias_suspenso, proporcional) {
    const tempoMin = document.querySelector('#tempoMin');
    const valorProporcional = document.querySelector('#valorProporcional');

    tempoMin.innerHTML = dias_suspenso;
    valorProporcional.innerHTML = proporcional;
}
