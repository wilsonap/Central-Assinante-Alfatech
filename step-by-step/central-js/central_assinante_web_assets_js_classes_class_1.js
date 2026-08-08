class HotsiteWeb {
    constructor() {
        this.slice = 12;
        this.home = false;
        app.cordova_app = false;
        document.addEventListener('deviceready', this.onDeviceReady, false);
    }

    onDeviceReady() {
        return app.cordova_app = true;
    }

    getUrlAppAndroid() {
        return app.url_app_android;
    }

    getIdCliente() {
        return this.id_cliente;
    }

    getConnectionsAcs() {
        const data = {
            ACTION: 'getDevices',
        };

        const criar_toast = this.criarToast;
        try {
            $.get(`${__SERVER__}/model/connections/connections.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    data = data;
                }

                if (data != '' && data != undefined) {
                    $(document).ready(() => {
                        app.loading = false;
                        router.history.current.matched[0].instances.default.connections = data;

                        validaActiveClass('#pg_connections');
                    });

                    return;
                }

                return app.loading = false;
            }).fail((e) => {
                router.history.current.matched[0].instances.default.connections = [];
                $(document).ready(() => {
                    app.loading = false;
                    router.history.current.matched[0].instances.default.loading = false;
                    validaActiveClass('#pg_connections');
                });
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'error_outline', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> getDevices() -> ${e}`;
        }
    }

    changedConfigWifi(configWifi) {
        const data = {
            CONFIG_WIFI: configWifi,
            ACTION: 'changedConfigWifi',
        };

        app.loading = true;
        const criar_toast = this.criarToast;
        try {
            $.get(`${__SERVER__}/model/connections/connections.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    data = data;
                }
                app.loading = false;
                $('#modalAlterarDevice').modal('hide');

                if (data != '' && data != undefined) {
                    if (data.type == 'success') {
                        criar_toast('2000', 'Sucesso!', 'Alteração no Wi-Fi está sendo processada. Isso pode levar alguns minutos para ser concluído. Por favor, aguarde.!', 'done', 'green', 'id_sucesso');
                    } else if (data.message) {
                        criar_toast('4000', 'Erro!', data.message, 'fas fa-times', 'red', 'id_erro');
                    } else {
                        criar_toast('4000', 'Erro!', 'Erro ao tentar executar a operaçao!.', 'fas fa-times', 'red', 'id_erro');
                    }
                }
            }).fail((e) => {
                app.loading = false;
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'error_outline', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> changedConfigWifi() -> ${e}`;
        }
    }

    rebootDevice(id_device) {
        const data = {
            ID_DEVICE: id_device || 0,
            ACTION: 'rebootDevice',
        };

        app.loading = true;
        const criar_toast = this.criarToast;
        try {
            $.get(`${__SERVER__}/model/connections/connections.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    data = data;
                }
                app.loading = false;
                if (data != '' && data != undefined) {
                    if (data.type == 'success') {
                        criar_toast('2000', 'Sucesso!', 'Dispositivo reiniciado com sucesso.', 'done', 'green', 'id_sucesso');
                    } else if (data.message) {
                        criar_toast('4000', 'Erro!', data.message, 'fas fa-times', 'red', 'id_erro');
                    } else {
                        criar_toast('4000', 'Erro!', 'Erro ao tentar executar a operaçao!.', 'fas fa-times', 'red', 'id_erro');
                    }
                }
            }).fail((e) => {
                app.loading = false;
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'error_outline', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> rebootDevice() -> ${e}`;
        }
    }

    initModalConnections(device) {
        app.data_wifi = {
            selectedWifi: '',
            ssid: '',
            passwordWifi1: '',
            passwordWifi2: '',
        };
        app.device_config_modal = device;
        $('.main-panel').css('opacity', '0.3');
        $('#modalAlterarDevice').on('hidden.bs.modal', () => {
            $('.main-panel').css('opacity', '1');
        });

        $('.topbar , .sidebar, #botaoPagarCreditCard, .botoes_fechar').click((e) => {
            $('#fecharAlterarConnections').click();
        });
    }

    getDadosServidor() {
        const data = {
            ACTION: 'getDadosServidor',
        };

        const criar_toast = this.criarToast;

        try {
            $.get(`${__SERVER__}/model/speed_test/speed_test.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    data = data;
                }

                if (data != '' && data != undefined) {
                    $(document).ready(() => {
                        const servidor = data.mensagem.SERVIDOR;
                        router.history.current.matched[0].instances.default.IP = data.mensagem.IP;
                        router.history.current.matched[0].instances.default.PingServer = servidor.servidor_ping;
                        router.history.current.matched[0].instances.default.SpeedTestServer = servidor.servidor_instalacao;
                        router.history.current.matched[0].instances.default.ServerName = servidor.servidor_instalacao;
                        app.showSpeedTest = !!servidor.servidor_instalacao;
                        router.history.current.matched[0].instances.default.SpeedTest = true;
                        router.history.current.matched[0].instances.default.loading = false;
                        router.history.current.matched[0].instances.default.iframe = true;
                        return app.loading = false;
                    });
                }
            }).fail((e) => {
                app.loading = false;
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            throw e;
        }
    }

    getDadosCarteira(idReceber, callbackSuccess) {
        const data = {
            ACTION: 'getDadosCarteira',
            ID_RECEBER: idReceber,
        };

        const criar_toast = this.criarToast;

        try {
            $.post(`${__SERVER__}/model/faturas/faturas.php`, data, callbackSuccess)
                .fail((e) => {
                    app.loading = false;
                    criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
                });
        } catch (e) {
            throw e;
        }
    }

    /**
     * Dados cliente
     */
    buscarDadosCliente() {
        const data = {
            ACTION: 'getDadosCliente',
        };

        const criar_toast = this.criarToast;

        try {
            $.get(`${__SERVER__}/model/dados_cliente/dados_cliente.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    data = data;
                }

                if (data != '' && data != undefined) {
                    router.history.current.matched[0].instances.default.dados_cliente_padrao = data.dados_cliente;
                    router.history.current.matched[0].instances.default.dados_cliente = data.dados_cliente;
                    router.history.current.matched[0].instances.default.paises = data.paises;
                    router.history.current.matched[0].instances.default.estados = data.estados;
                    router.history.current.matched[0].instances.default.cidades = data.cidades;
                    router.history.current.matched[0].instances.default.mostrar_tela = 'A';
                    router.history.current.matched[0].instances.default.cliente_nome = data.dados_cliente.fantasia;

                    router.history.current.matched[0].instances.default.habilitar_altera_senha = data.dados_cliente.habilitar_altera_senha;
                    $(document).ready(() => {
                        const hs_web = new HotsiteWeb();
                        hs_web.buscarLogo();

                        app.loading = false;
                        router.history.current.matched[0].instances.default.loading = false;
                        router.history.current.matched[0].instances.default.mostrar_privacidade = !app.mostra_builder;

                        setTimeout(() => {
                            const acessoAutomatico = document.getElementById('acesso_automatico');
                            if (typeof acessoAutomatico !== 'undefined' && acessoAutomatico != null) {
                                if (data.dados_cliente.acesso_automatico_central === 'S') {
                                    acessoAutomatico.checked = true;
                                } else {
                                    acessoAutomatico.checked = false;
                                }
                            }
                        });

                        hs_web.setarPermissaoDadosCliente();
                    });
                    return;
                }

                router.history.current.matched[0].instances.default.dados_cliente_padrao = [];
                router.history.current.matched[0].instances.default.dados_cliente = [];
                router.history.current.matched[0].instances.default.paises = [];
                router.history.current.matched[0].instances.default.estados = [];
                router.history.current.matched[0].instances.default.cidades = [];
            }).fail((e) => {
                app.loading = false;
                router.history.current.matched[0].instances.default.loading = false;
                router.history.current.matched[0].instances.default.dados_cliente_padrao = [];
                router.history.current.matched[0].instances.default.dados_cliente = [];
                router.history.current.matched[0].instances.default.paises = [];
                router.history.current.matched[0].instances.default.estados = [];
                router.history.current.matched[0].instances.default.cidades = [];
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> buscarDadosCliente() -> ${e}`;
        }
    }

    updateFirebaseToken() {
        if (window.ReactNativeWebView) {
            this.getTokenAPP()
                .then((token) => {
                    $.post(`${__SERVER__}/model/dados_cliente/dados_cliente.php`, { ACTION: 'updateFirebaseToken', TOKEN: token }, () => {})
                        .fail(() => {});
                });
        }
    }

    getCidades(estado) {
        const data = {
            ACTION: 'getCidades',
            ESTADO: estado || 0,
        };

        const hs_web = new HotsiteWeb();

        try {
            $.get(`${__SERVER__}/model/dados_cliente/dados_cliente.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    data = data;
                }

                if (data != '' && data != undefined) {
                    router.history.current.matched[0].instances.default.cidades = data;

                    $(document).ready(() => {
                        app.loading = false;
                        router.history.current.matched[0].instances.default.loading = false;
                        router.history.current.matched[0].instances.default.dados_cliente.cidade_nome = '';
                        $("input[name='cidades']").focus();
                    });
                    return;
                }
                router.history.current.matched[0].instances.default.cidades = [];
            }).fail((e) => {
                app.loading = false;
                router.history.current.matched[0].instances.default.cidades = [];
                hs_web.criarToast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> buscarDadosCliente() -> ${e}`;
        }
    }

    solicitarAtualizacaoCadastro(dados) {
        const data = {
            ACTION: 'setDadosClienteAlteracao',
            DADOS_ALTERACAO: dados || '',
        };

        const hs_web = new HotsiteWeb();

        try {
            $.post(`${__SERVER__}/model/dados_cliente/dados_cliente.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    data = data;
                }

                if (data != '' && data != undefined) {
                    if (data.tipo === 'sucesso') {
                        hs_web.criarToast('3000', 'Sucesso!', data.mensagem, 'fas fa-check', 'green', 'id_sucesso');
                        router.history.current.matched[0].instances.default.atualizando_dados = false;
                        hs_web.buscarDadosCliente();
                    } else {
                        hs_web.criarToast('5000', 'Erro!', data.mensagem, 'fas fa-exclamation-circle', 'red', 'id_erro');
                        $(document).ready(() => {
                            app.loading = false;
                        });
                    }
                }
            }).fail((e) => {
                app.loading = false;
                hs_web.criarToast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> buscarDadosCliente() -> ${e}`;
        }
    }

    alterarSenha(senha_atual, senha_nova, senha_nova_repetir) {
        const data = {
            SENHA_ATUAL: senha_atual || '',
            SENHA_NOVA: senha_nova || '',
            SENHA_NOVA_REPETIR: senha_nova_repetir || '',
            ACTION: 'setAlterarSenha',
        };

        const criar_toast = this.criarToast;

        try {
            $.post(`${__SERVER__}/model/dados_cliente/dados_cliente.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    data = data;
                }
                if (data != '' && data != undefined) {
                    if (data[0].tipo == 'sucesso') {
                        router.history.current.matched[0].instances.default.dados_cliente.senha_atual = '';
                        router.history.current.matched[0].instances.default.dados_cliente.senha_nova = '';
                        router.history.current.matched[0].instances.default.dados_cliente.senha_nova_repetir = '';
                        router.history.current.matched[0].instances.default.atualizando_dados_senha = false;
                        criar_toast('2000', 'Sucesso!', 'Senha alterada com sucesso.', 'fas fa-check', 'green', 'id_sucesso');
                    } else {
                        router.history.current.matched[0].instances.default.atualizando_dados_senha = false;
                        criar_toast('4000', 'Erro!', `${data[0].mensagem}.`, 'fas fa-exclamation-circle', 'red', 'id_erro');
                    }
                }
            }).fail((e) => {
                router.history.current.matched[0].instances.default.atualizando_dados_senha = false;
                router.history.current.matched[0].instances.default.dados_cliente.senha_atual = '';
                router.history.current.matched[0].instances.default.dados_cliente.senha_nova = '';
                router.history.current.matched[0].instances.default.dados_cliente.senha_nova_repetir = '';
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> alterarSenha() -> ${e}`;
        }
    }

    setSenhaAtualParaMD5(senha) {
        const data = {
            SENHA_ATUAL: senha || '',
            ACTION: 'criptografarSenhaParaMD5',
        };
        const toast = this.criarToast;

        try {
            $.get(`${__SERVER__}/model/dados_cliente/dados_cliente.php`, data, (data) => {
                data = JSON.parse(data);

                if (data.criptografada === 'S' && senha !== '') {
                    const campoSenhaAtual = document.getElementById('senha_atual');
                    campoSenhaAtual.value = data.message;
                    router.history.current.matched[0].instances.default.dados_cliente.senha_atual = data.message;
                    toast('5000', 'Sucesso!', 'Como a opção "Senha codificada" está ativada no cadastro do cliente, a senha digitada também foi criptografada!', 'fas fa-check', 'green', 'id_sucesso');
                }
            }).fail((e) => {
                toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> retornarSenhaMD5() -> ${e}`;
        }
    }

    cadastrarNovoCliente(array_dados) {
        const data = {
            NOME: array_dados.nome,
            TELEFONE: array_dados.telefone,
            CPF_CNPJ: array_dados.cpf_cnpj,
            EMAIL: array_dados.email,
            ACTION: 'getValidaCadastro',
        };

        const criar_toast = this.criarToast;

        try {
            $.get(`${__SERVER__}/model/login/login.php`, data, (data) => {
                data = JSON.parse(data);

                if (data != '' && data != undefined) {
                    if (Array.isArray(data)) {
                        const countErros = data.length;
                        let i = 0;
                        while (i <= countErros) {
                            criar_toast('5000', 'Erro!', data[i].mensagem, 'fas fa-exclamation-circle', 'red', 'id_erro', false);
                            i++;
                        }
                    } else if (data.tipo == 'sucesso') {
                        criar_toast('3000', 'Sucesso!', data.mensagem, 'fas fa-check', 'green', 'id_sucesso');
                        setTimeout("location.href = '/central_assinante_web/login';", 3000);
                    } else {
                        criar_toast('5000', 'Erro!', data.mensagem, 'fas fa-exclamation-circle', 'red', 'id_erro');
                    }
                }
            }).fail((e) => {
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> enviarNovoAtendimento() -> ${e}`;
        }
    }

    validarEmail(email) {
        let valid = true;
        const emails = email.replace(';', ',').split(',');

        jQuery.each(emails, function () {
            if (jQuery.trim(this) != '') {
                if (!jQuery.trim(this).match(/^([\w\.\-]+)@([\w\-]+)((\.(\w){2,3})+)$/i)) valid = false;
            }
        });
        return valid;
    }

    habilitarDesabilitarAcessoAutomatico(acesso_automatico) {
        const data = {
            ACESSO_AUTOMATICO: acesso_automatico ? 'S' : 'N',
            ACTION: 'setHabilitarDesabilitarAcessoAutomatico',
        };

        const hs_web = new HotsiteWeb();

        try {
            $.get(`${__SERVER__}/model/dados_cliente/dados_cliente.php`, data, (data) => {
                data = JSON.parse(data);
                if (data != '' && data != undefined) {
                    $(document).ready(() => {
                        app.loading = false;
                    });

                    if (data.tipo === 'sucesso') {
                        hs_web.criarToast('3000', 'Sucesso!', data.mensagem, 'fas fa-check', 'green', 'id_sucesso');
                    } else if (acesso_automatico) {
                        hs_web.criarToast('5000', 'Erro!', 'Ocorreu um erro ao tentar ativar Acesso Automático', 'fas fa-exclamation-circle', 'red', 'id_erro');
                    } else {
                        hs_web.criarToast('5000', 'Erro!', 'Ocorreu um erro ao tentar desativar Acesso Automático', 'fas fa-exclamation-circle', 'red', 'id_erro');
                    }
                }
            }).fail((e) => {
                hs_web.criarToast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> enviarNovoAtendimento() -> ${e}`;
        }
    }

    /**
     * Faturas
     */
    buscarFaturas(slice, home = false, pg_faturas, valida_loading) {
        const data = {
            SLICE: slice || this.slice,
            HOME: home || this.home,
            ACTION: 'getFaturas',
        };
        const hs_web = new HotsiteWeb();

        const criar_toast_pendencia = this.criarToastPendencia;
        const valida_resize_home = this.validaResizeHome;
        const valida_resize_faturas = this.validaResizeFaturas;
        const valida_loading_function = this.validaLoading;
        try {
            $.get(`${__SERVER__}/model/faturas/faturas.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    data = data;
                }
                if (data != '' && data != undefined) {
                    router.history.current.matched[0].instances.default.faturas = data;
                    router.history.current.matched[0].instances.default.is_safari = IS_SAFARI;

                    if (data.total_registros <= SLICE_FATURA) {
                        router.history.current.matched[0].instances.default.mostrar_cinco_faturas = true;
                    } else {
                        router.history.current.matched[0].instances.default.mostrar_cinco_faturas = false;
                    }

                    if (pg_faturas) {
                        valida_resize_faturas();
                    } else {
                        valida_resize_home();
                    }

                    $(document).ready(() => {
                        if (valida_loading) {
                            LOADING_FATURA = false;
                            valida_loading_function();
                        } else {
                            app.loading = false;
                            router.history.current.matched[0].instances.default.loading = false;

                            hs_web.setarPermissaoFaturas();

                            validaActiveClass('#pg_fatura');
                        }
                    });
                    return;
                }

                router.history.current.matched[0].instances.default.faturas = [];

                $(document).ready(() => {
                    if (valida_loading) {
                        LOADING_FATURA = false;
                        valida_loading_function();
                    } else {
                        app.loading = false;
                        router.history.current.matched[0].instances.default.loading = false;
                    }
                });
            }).fail((e) => {
                $(document).ready(() => {
                    if (valida_loading) {
                        LOADING_FATURA = false;
                        valida_loading_function();
                    } else {
                        app.loading = false;
                        router.history.current.matched[0].instances.default.loading = false;
                    }
                });

                router.history.current.matched[0].instances.default.loading = false;
                router.history.current.matched[0].instances.default.faturas = [];
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> buscarFaturas() -> ${e}`;
        }
    }

    enviarFaturaSMS(tipo, id_receber) {
        const data = {
            ID_RECEBER: id_receber || 0,
            TIPO: 'SMS',
            ACTION: 'setBoletoSMS',
        };

        app.loading = true;

        const criar_toast = this.criarToast;

        try {
            $.get(`${__SERVER__}/model/faturas/faturas.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    data = data;
                }
                app.loading = false;
                if (data != '' && data != undefined) {
                    if (data[0].tipo == 'sucesso') {
                        criar_toast('2000', 'Sucesso!', 'A fatura foi enviada por SMS para seu celular.', 'fas fa-check', 'green', 'id_sucesso');
                    } else {
                        criar_toast('2000', 'Erro!', 'Ocorreu um erro ao enviar a fatura.', 'fas fa-exclamation-circle', 'red', 'id_erro');
                    }
                }
            }).fail((e) => {
                app.loading = false;
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> enviarFaturaSMS() -> ${e}`;
        }
    }

    enviarFaturaEMAIL(id_receber) {
        const data = {
            ID_RECEBER: id_receber || 0,
            ACTION: 'setBoletoEMAIL',
        };

        app.loading = true;
        const criar_toast = this.criarToast;
        try {
            $.get(`${__SERVER__}/model/faturas/faturas.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    data = data;
                }

                app.loading = false;
                if (data != '' && data != undefined) {
                    if (data.tipo == 'sucesso') {
                        criar_toast('2000', 'Sucesso!', 'A fatura foi enviada para seu email.', 'fas fa-check', 'green', 'id_sucesso');
                    } else if (data[0].mensagem) {
                        criar_toast('4000', 'Erro!', data[0].mensagem, 'fas fa-exclamation-circle', 'red', 'id_erro');
                    } else {
                        criar_toast('4000', 'Erro!', 'Ocorreu um erro ao enviar a fatura.', 'fas fa-exclamation-circle', 'red', 'id_erro');
                    }
                }
            }).fail((e) => {
                app.loading = false;
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> enviarFaturaEMAIL() -> ${e}`;
        }
    }

    restoreModalVariavel() {
        app.modal_url = '';
        app.link_fatura = '';
        app.pdf_fatura_nota = '';
    }

    imprimirFatura(id_receber, criar_toast, alt_gateway = false) {
        app.modal_url = '';
        this.restoreModalVariavel();

        const data = {
            ID_RECEBER: id_receber || 0,
            APP: app.cordova_app ? 'S' : 'N',
            ACTION: 'getBoletoArquivo',
            ALT_GATEWAY: (alt_gateway === true),
        };

        $('#btn_imprimir_fat_').attr('disabled', true);

        app.loading_modal = true;
        var criar_toast = criar_toast || this.criarToast;

        try {
            if (data.APP === 'S') {
                app.loading = true;

                let permiteDownload = true;

                /**
                 *  Validar se o Boleto eh um Link Gateway para mostrar o modal
                 * */
                $.ajax({
                    url: `${__SERVER__}/model/faturas/faturas.php`,
                    async: false,
                    type: 'GET',
                    data,
                }).done((data) => {
                    try {
                        data = JSON.parse(data);
                    } catch (e) {
                        data = data;
                    }

                    if (data[0].tipo == 'link') {
                        permiteDownload = false;
                        $('#btn_imprimir_fat_').attr('disabled', false);
                        app.loading = false;
                        app.modal_url = '';
                        app.loading_modal = false;

                        cordova.InAppBrowser.open(
                            data[0].mensagem,
                            '_blank',
                            'location=yes',
                        );
                    }
                }).fail(() => {
                    app.loading = false;
                    app.modal_url = '';
                    app.loading_modal = false;
                    criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
                });

                if (permiteDownload) {
                    const fileTransfer = new FileTransfer();
                    const fileURL = `${cordova.file.dataDirectory}fatura.pdf`;
                    const uri = `${__SERVER__}/model/faturas/faturas.php?ID_RECEBER=${data.ID_RECEBER}&APP=${data.APP}&ACTION=${data.ACTION}`;

                    fileTransfer.download(uri, fileURL,
                        (entry) => {
                            cordova.plugins.fileOpener2.open(entry.toURL(), 'application/pdf', {
                                error(e) {
                                    app.loading = false;
                                    alert('Instale um leitor de arquivo .pdf!');
                                },
                                success(e) {
                                    app.loading = false;
                                },
                            });
                        },
                        (e) => {
                            app.loading = false;
                            alert('Erro ao baixar arquivo!');
                        }, false, {});

                    $('#btn_imprimir_fat_').attr('disabled', false);
                }
            } else {
                $.get(`${__SERVER__}/model/faturas/faturas.php`, data, (data) => {
                    try {
                        data = JSON.parse(data);
                    } catch (e) {
                        data = data;
                    }
                    if (data[0].tipo !== 'erro') {
                        if (data[0].tipo == 'link') {
                            $('#btn_imprimir_fat_').attr('disabled', false);
                            return app.link_fatura = data[0].mensagem;
                        }
                        if (typeof window.ReactNativeWebView !== 'undefined') {
                            postMessageMobile('base64', JSON.stringify({
                                base: data[0].mensagem.base_pdf,
                                nome: btoa((Math.floor(Math.random() * 9999) + 1000)) + data[1],
                            })).then(
                                (retorno) => {
                                    $('#btn_imprimir_fat, #btn_imprimir_fat_, #btn_imprimir_fat_home, #btn_imprimir_fat_home_').attr('disabled', false);
                                    document.getElementById('modalImpressaoClose').click();
                                    if (JSON.parse(retorno.data).retorno == 'success') {
                                        criar_toast('2000', 'Sucesso!', 'O seu download foi completado com êxito', 'fas fa-check', 'green', 'id_sucesso');
                                    } else {
                                        criar_toast('2000', 'Erro!', 'Ocorreu algum erro ao fazer o download', 'fas fa-exclamation-circle', 'red', 'id_erro');
                                    }
                                },
                            );
                        } else {
                            if (data[2] == 'true') {
                                $('#btn_imprimir_fat_').attr('disabled', false);

                                const xmlHttp = new XMLHttpRequest();
                                xmlHttp.open('GET', `${__SERVER__}/model/faturas/faturas.php?APP=N&ACTION=retornarArquivo&CAMINHO=${data[1]}`, true);
                                xmlHttp.responseType = 'blob';
                                xmlHttp.onreadystatechange = function (e) {
                                    if (xmlHttp.readyState === 4 && (xmlHttp.status === 200 || xmlHttp.status === 0)) {
                                        const link = document.createElement('a');
                                        link.href = URL.createObjectURL(xmlHttp.response);
                                        link.download = 'Fatura.pdf';
                                        link.click();

                                        document.getElementById('modalImpressaoClose').click();
                                    }
                                };
                                xmlHttp.send();
                            }
                            app.loading_modal = false;
                            if (data != '' && data != undefined && !navigator.userAgent.match(/Android|BlackBerry|iPhone|iPad|iPod|IEMobile/i)) {
                                if (data[0].tipo == 'sucesso') {
                                    $('#btn_imprimir_fat_').attr('disabled', false);
                                    return app.modal_url = data[0].mensagem.base_pdf;
                                }
                            }
                        }
                    } else {
                        if (data[0].mensagem) {
                            criar_toast('4000', 'Erro!', data[0].mensagem, 'fas fa-exclamation-circle', 'red', 'id_erro');

                            app.loading_modal = false;
                        } else {
                            criar_toast('2000', 'Aviso!', 'Ocorreu um erro ao imprimir a fatura.', 'fas fa-exclamation-circle', 'red', 'id_erro');
                        }
                        $('#btn_imprimir_fat_').attr('disabled', false);
                    }
                }).fail((e) => {
                    app.modal_url = '';
                    app.loading_modal = false;
                    criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
                    $('#btn_imprimir_fat_').attr('disabled', false);
                });
            }
        } catch (e) {
            return `Exception Class HotsiteWeb -> imprimirFatura() -> ${e}`;
        }
    }

    suspenderContrato(id_contrato, data_inicial_suspensao, data_final_suspensao) {
        const closeButton = document.querySelector('#modalConcluirSuspensao button[data-dismiss=modal]');
        closeButton.click();
        const criar_toast = this.criarToast;
        const data = {
            ID_CONTRATO: id_contrato || 0,
            DATA_INICIAL_SUSPENSAO: data_inicial_suspensao || 0,
            DATA_FINAL_SUSPENSAO: data_final_suspensao || 0,
            ACTION: 'suspenderContrato',
        };
        $.ajax({
            url: `${__SERVER__}/model/planos/planos.php`,
            async: true,
            type: 'GET',
            data,
        }).done((data) => {
            try {
                closeButton.click();
                data = JSON.parse(data);

                if (data.tipo === 'S') {
                    new HotsiteWeb().buscarPlanos(SLICE_PLANO, false, '', true);
                    criar_toast('5000', 'Sucesso!', data.resp, 'fas fa-check', 'green', 'id_sucesso');
                } else {
                    criar_toast('5000', 'Erro!', data.resp, 'fas fa-exclamation-circle', 'red', 'id_erro');
                }
            } catch (e) {
                data = data;
            }
        }).fail(() => {
            criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
        });
    }

    removerSuspensaoContrato(id_contrato) {
        const closeButton = document.querySelector('#modalRemoveSuspensaoConcluir button[data-dismiss=modal]');
        closeButton.click();
        const criar_toast = this.criarToast;
        const data = {
            ID_CONTRATO: id_contrato || 0,
            ACTION: 'removerSuspensaoContrato',
        };
        $.ajax({
            url: `${__SERVER__}/model/planos/planos.php`,
            async: true,
            type: 'GET',
            data,
        }).done((data) => {
            try {
                closeButton.click();
                data = JSON.parse(data);

                if (data.tipo === 'S') {
                    new HotsiteWeb().buscarPlanos(SLICE_PLANO, false, '', true);

                    criar_toast('5000', 'Sucesso!', data.resp, 'fas fa-check', 'green', 'id_sucesso');
                } else {
                    criar_toast('5000', 'Erro!', data.resp, 'fas fa-exclamation-circle', 'red', 'id_erro');
                }
            } catch (e) {
                data = data;
            }
        }).fail(() => {
            criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
        });
    }

    imprimirPix(id_receber) {
        const criar_toast = this.criarToast;
        criar_toast('25000', '', 'Caso você já tenha efetuado o pagamento por outro meio, aguarde seu processamento!', '', 'yellow', '');

        const headerPix = document.getElementById('headerPix');
        const pixLoading = document.getElementById('pixLoading');
        const qrCodePix = document.getElementById('qrCodePix');
        const pixFooter = document.getElementById('pixFooter');
        const pixCopiaECola = document.getElementById('qrCodePixCopiaECola');
        const botaoFecharModal = document.getElementById('fecharConsultarFatura');
        let botaoImprimirPixConsultarFatura = '';
        if (document.getElementById('botaoImprimirPixConsultarFatura')) {
            botaoImprimirPixConsultarFatura = document.getElementById('botaoImprimirPixConsultarFatura');
        } else if (document.getElementById('btn_imprimir_pix')) {
            botaoImprimirPixConsultarFatura = document.getElementById('btn_imprimir_pix');
        } else if (document.getElementById('btn_imprimir_pix_home')) {
            botaoImprimirPixConsultarFatura = document.getElementById('btn_imprimir_pix_home');
        } else if (document.getElementById('btn_imprimir_pix_home_')) {
            botaoImprimirPixConsultarFatura = document.getElementById('btn_imprimir_pix_home_');
        } else if (document.getElementById('btn_imprimir_pix_')) {
            botaoImprimirPixConsultarFatura = document.getElementById('btn_imprimir_pix_');
        }
        const modalPix = document.getElementById('modalPix');

        pixLoading.style.display = 'block';
        headerPix.style.display = 'none';
        qrCodePix.style.display = 'none';
        pixFooter.style.display = 'none';
        pixCopiaECola.style.display = 'none';

        botaoFecharModal.click();
        botaoImprimirPixConsultarFatura.setAttribute('disabled', '');

        let success = false;

        const data = {
            ID_RECEBER: id_receber || 0,
            ACTION: 'getDadosPix',
        };

        $.ajax({
            url: `${__SERVER__}/model/faturas/faturas.php`,
            async: false,
            type: 'GET',
            data,
        }).done((data) => {
            pixLoading.style.display = 'none';
            modalPix.style.overflow = 'scroll';
            try {
                data = JSON.parse(data);

                if (data.type === 'success') {
                    success = true;
                    headerPix.style.display = 'block';
                    headerPix.style.textAlign = 'left';
                    qrCodePix.style.display = 'block';
                    pixFooter.style.display = 'block';
                    pixCopiaECola.style.display = 'block';

                    headerPix.innerHTML = `<p><b>${data.pix.dadosPix.devedor.nome}</b><\p>`
                        + `<p><b>Valor: </b>R$ ${data.pix.dadosPix.valor.original}<\p>`
                        + `<p><b>Código PIX válido até: </b>${data.pix.dadosPix.expiracaoPix} (Horário de Brasília)<\p>`
                        + `<p style="text-align: center">${data.pix.dadosPix.solicitacaoPagador}</p>`;
                    if (data.gateway.gatewayNome == 'fortunus') {
                        qrCodePix.innerHTML = `<img id="imgQrCode" src="data:image/svg+xml;base64,${data.pix.qrCode.imagemQrcode}" style="max-width: 200px; max-height: 200px;"></center>`;
                    } else {
                        qrCodePix.innerHTML = `<img id="imgQrCode" src="data:image/png;base64,${data.pix.qrCode.imagemQrcode}" style="max-width: 200px; max-height: 200px;"></center>`;
                    }

                    pixCopiaECola.innerHTML = `Pix Copia e Cola: <input readonly="readonly" id="inputPixCopiaECola" value="${data.pix.qrCode.qrcode}"> <button id="botaoCopiarPix">Copiar</button><br><br>`
                        + '<p><b id="counter">00:00</b></p>';

                    const botaoCopiarPix = document.getElementById('botaoCopiarPix');
                    botaoCopiarPix.addEventListener('click', () => {
                        const inputPixCopiaECola = document.getElementById('inputPixCopiaECola');

                        const displaySuccessToast = () => criar_toast(
                            2000,
                            'Sucesso!',
                            'Chave Pix copiada para a área de transferência.',
                            'fas fa-check',
                            'green',
                            'id_sucesso',
                            true,
                        );

                        const displayErrorToast = () => criar_toast(
                            2000,
                            'Erro!',
                            'Não foi possível copiar a chave pix.',
                            'fas fa-times',
                            'red',
                            'id_erro',
                            true
                        );

                        navigator.clipboard.writeText(inputPixCopiaECola?.value || '').then(() => {
                            const isClipboardTextMatching = (clipboardText) => clipboardText === inputPixCopiaECola?.value;
                            navigator.clipboard.readText().then((clipboardText) => {
                                if (isClipboardTextMatching(clipboardText)) {
                                    displaySuccessToast();
                                } else {
                                    displayErrorToast();
                                }
                            });
                        }).catch(displayErrorToast);
                    });

                    pixFooter.innerHTML = '<p>Utilize o app do seu banco para escanear o código <b>PIX</b> e concluir o pagamento.<br>';

                    if (data.gateway.gatewayNome == 'sicoobApi') {
                        pixFooter.innerHTML = '<p><b>ATENÇÃO</b>: O processamento da transação via PIX pode demorar <b>até 1 dia útil</b> para ser efetivada.<br><br>';
                    }
                } else {
                    headerPix.style.display = 'block';
                    headerPix.style.textAlign = 'center';
                    headerPix.innerHTML = `${'<img src="data:image/png;base64,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"/>'
                        + '</center><p>'}${data.mensagem}</p>`;
                    criar_toast('5000', 'Erro!', data.mensagem, 'fas fa-exclamation-circle', 'red', 'id_erro');
                }
            } catch (e) {
                data = data;
            }
        }).fail(() => {
            criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
        });

        if (success) {
            this.consultarStatusPix(id_receber);
        }
        botaoImprimirPixConsultarFatura.removeAttribute('disabled');
    }

    consultarStatusPix(id_receber) {
        const criar_toast = this.criarToast;

        let mm = 4;
        let ss = 59;

        const tempo = 1000;

        const cron = setInterval(() => { timer(); }, tempo);

        function timer() {
            ss--;

            if (ss === 0) {
                if (mm === 0) {
                    document.getElementById('counter').innerText = '00:00';
                    clearInterval(cron);
                    return;
                }
                ss = 59;
                mm--;
            }

            document.getElementById('counter').innerText = (`0${mm}:${ss < 10 ? `0${ss}` : ss}`);
        }

        const data = {
            ID_RECEBER: id_receber || 0,
            ACTION: 'getStatusPix',
        };

        $.ajax({
            url: `${__SERVER__}/model/faturas/faturas.php`,
            async: true,
            type: 'GET',
            data,
            timeout: 296000,
        }).done((data) => {
            try {
                data = JSON.parse(data);

                if (data.type === 'success') {
                    const imgQrCode = document.getElementById('imgQrCode');
                    const qrCodePix = document.getElementById('qrCodePix');
                    const pixCopiaECola = document.getElementById('qrCodePixCopiaECola');
                    pixCopiaECola.style.display = 'none';
                    qrCodePix.removeChild(imgQrCode);

                    qrCodePix.innerHTML = '<svg id="payedPix" version="1.1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 130.2 130.2" style="max-height: 200px">\n'
                        + '  <circle class="path circle" fill="none" stroke="#77B6A8" stroke-width="6" stroke-miterlimit="10" cx="65.1" cy="65.1" r="62.1"/>\n'
                        + '  <polyline class="path check" fill="none" stroke="#77B6A8" stroke-width="6" stroke-linecap="round" stroke-miterlimit="10" points="100.2,40.2 51.5,88.8 29.8,67.5 "/>\n'
                        + '</svg><br><br>'
                        + '<p class="success" style="color: #77B6A8; text-align: center; font-size: 1.25em;">Seu pagamento foi confirmado!</p>';
                } else {
                    const pixFooter = document.getElementById('pixFooter');
                    const pixCopiaECola = document.getElementById('qrCodePixCopiaECola');
                    const qrCodePix = document.getElementById('qrCodePix');
                    const imgQrCode = document.getElementById('imgQrCode');
                    pixFooter.style.display = 'none';
                    imgQrCode.style.display = 'none';
                    pixCopiaECola.style.display = 'none';
                    qrCodePix.style.textAlign = 'center';
                    qrCodePix.innerHTML = `${'<img src="data:image/png;base64,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"/>'
                        + '</center><p>'}${data.mensagem}</p>`;
                    criar_toast('5000', 'Erro!', data.mensagem, 'fas fa-exclamation-circle', 'red', 'id_erro');
                }
            } catch (e) {

            }
        }).fail(() => {
            const pixFooter = document.getElementById('pixFooter');
            const pixCopiaECola = document.getElementById('qrCodePixCopiaECola');
            const qrCodePix = document.getElementById('qrCodePix');
            const imgQrCode = document.getElementById('imgQrCode');
            pixFooter.style.display = 'none';
            imgQrCode.style.display = 'none';
            pixCopiaECola.style.display = 'none';
            qrCodePix.style.textAlign = 'center';
            qrCodePix.innerHTML = '<img src="data:image/png;base64,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"/>'
                + '</center><p>Tempo para leitura do QrCode expirado. Por favor recarregue a página!</p>';
            criar_toast('5000', 'Erro!', 'Tempo para leitura do QrCode expirado. Por favor recarregue a página!', 'fas fa-exclamation-circle', 'red', 'id_erro');
        });
    }

    imprimirQuitacaoDebitos(ano_selecionado) {
        this.restoreModalVariavel();

        const data = {
            APP: app.cordova_app ? 'S' : 'N',
            ACTION: 'getQuitacaoDebito',
            ANO_CONTRATO: ano_selecionado,
        };

        const criar_toast = this.criarToast;
        app.loading_modal = true;

        try {
            if (data.APP === 'S') {
                app.loading = true;

                $.get(`${__SERVER__}/model/faturas/faturas.php`, { APP: 'N', ACTION: 'getQuitacaoDebito' }, (data) => {
                    try {
                        data = JSON.parse(data);
                    } catch (e) {
                        data = data;
                    }
                    if (data[0].tipo === 'sucesso') {
                        if (__REACT_NATIVE__) {
                            window.ReactNativeWebView.postMessage(JSON.stringify({
                                type: 'download',
                                url: encodeURI(`${__SERVER__}/model/faturas/faturas.php?APP=S&ACTION=getQuitacaoDebito&ANO_CONTRATO=${ano_selecionado}`),
                                sessao: $.cookie('sessao'),
                            }));
                        } else {
                            const fileTransfer = new FileTransfer();
                            const fileURL = `${cordova.file.dataDirectory}quitacao_debito.pdf`;
                            const uri = `${__SERVER__}/model/faturas/faturas.php?APP=S&ACTION=getQuitacaoDebito&ANO_CONTRATO=${ano_selecionado}`;

                            fileTransfer.download(uri, fileURL,
                                (entry) => {
                                    cordova.plugins.fileOpener2.open(entry.toURL(), 'application/pdf', {
                                        error(e) {
                                            app.loading = false;
                                            alert('Instale um leitor de arquivo .pdf!');
                                        },
                                        success(e) {
                                            app.loading = false;
                                        },
                                    });
                                },
                                (e) => {
                                    app.loading = false;
                                    alert('Erro ao baixar arquivo!');
                                }, false, {});
                        }
                    } else {
                        app.loading = false;
                        return criar_toast('4000', 'Erro!', data[0].mensagem, 'fas fa-exclamation-circle', 'red', 'id_erro');
                    }
                }).fail((e) => {
                    app.modal_url = '';
                    app.loading = false;
                    app.loading_modal = false;
                    return criar_toast('4000', 'Erro!', data[0].mensagem, 'fas fa-exclamation-circle', 'red', 'id_erro');
                });
            } else {
                $.get(`${__SERVER__}/model/faturas/faturas.php`, data, (data) => {
                    if (typeof window.ReactNativeWebView !== 'undefined') {
                        data = JSON.parse(data);
                        $('#btn_imprimir_fat, #btn_imprimir_fat_, #btn_imprimir_fat_home, #btn_imprimir_fat_home_').attr('disabled', false);
                        $('#modalImpressaoClose').click();
                        postMessageMobile('base64', JSON.stringify({
                            base: data[0].mensagem.base_pdf,
                            nome: btoa(`${Math.floor(Math.random() * 9999) + 1000}arquivo.pdf`),
                        })).then(
                            (retorno) => {
                                $('#btn_imprimir_fat_').attr('disabled', false);
                                if (JSON.parse(retorno.data).retorno == 'success') {
                                    criar_toast('2000', 'Sucesso!', 'O seu download foi completado com êxito', 'fas fa-check', 'green', 'id_sucesso');
                                } else {
                                    return criar_toast('4000', 'Erro!', data[0].mensagem, 'fas fa-exclamation-circle', 'red', 'id_erro');
                                }
                            },
                        );
                    } else {
                        if (data != '<strong>TCPDF ERROR: </strong>[Image] Unable to get the size of the image: /var/www/includes/../') {
                            data = JSON.parse(data);
                        }

                        app.loading_modal = false;
                        if (data[0].tipo == 'sucesso') {
                            if (navigator.userAgent.match(/Android|BlackBerry|iPhone|iPad|iPod|IEMobile/i)) {
                                const link = document.createElement('a');
                                link.href = `data:application/octet-stream;base64,${data[0].mensagem.base_pdf}`;
                                link.download = 'QuitacaoDebito.pdf';
                                link.click();
                                $('#modalImpressaoClose').click();
                            } else {
                                return app.modal_url = data[0].mensagem.base_pdf;
                            }
                        } else {
                            return criar_toast('4000', 'Erro!', data[0].mensagem, 'fas fa-exclamation-circle', 'red', 'id_erro');
                        }
                    }
                }).fail((e) => {
                    app.modal_url = '';
                    app.modal_nome = '';
                    app.loading_modal = false;
                    criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
                });
            }
        } catch (e) {
            return `Exception Class HotsiteWeb -> imprimirQuitacaoDebitos() -> ${e}`;
        }
    }

    instanciarCartao() {
        new Card({
            form: document.querySelector('.form-pagamento'),
            container: '.card-wrapper',
            placeholders: {
                number: '•••• •••• •••• ••••',
                name: 'Nome completo',
                expiry: '••/••••',
                cvc: '•••',
            },
        });
    }

    ativarRecorrenciaVindi(id_contrato, numero_cartao, mes, ano, dv, cartaoNome, bandeira) {
        const criar_toast = this.criarToast;

        const data = {
            ID_CONTRATO: id_contrato || 0,
            NUMERO_CARTAO: numero_cartao || 0,
            MES: mes || 0,
            ANO: ano || 0,
            DV: dv || 0,
            BANDEIRA: bandeira || '',
            CARTAO_NOME: cartaoNome || '',
            ACTION: 'ativarRecorrenciaVindi',
        };

        try {
            $.post(`${__SERVER__}/model/faturas/faturas.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    data = data;
                }
                const response = data[0];
                if (response.tipo === 'sucesso') {
                    criar_toast(10000, 'Sucesso!', response.mensagem, 'fas fa-check', 'green', 'id_sucesso', true);
                    setTimeout("location.href = '/central_assinante_web/planos';", 3000);
                } else {
                    const { mensagem } = response;
                    criar_toast(3000, 'Ops!', mensagem, 'fas fa-exclamation-circle', 'red', 'id_erro', false);
                }
                console.log(data);
                app.loading = false;
            }).fail((e) => {
                criar_toast('3000', 'Erro!', 'Ocorreu um erro no servidor. Por favor entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
                setTimeout("location.href = '/central_assinante_web/planos';", 3000);
            });
            function noBack() { window.history.forward(); }
            noBack();
            window.onload = noBack;
            window.onpageshow = function (evt) { if (evt.persisted)noBack(); };
            window.onunload = function () { void (0); };
        } catch (e) {
            return `Exception Class HotsiteWeb -> ativarRecorrenciaVindi() -> ${e}`;
        }
    }

    pagarFatura(id_receber, parcelas, numero_cartao, mes, ano, bandeira, dv, recorrente, cartaoNome, creditCardAmbiente, creditcardGateway, gerenciaIdentificadorConta, paymentToken = '', dataEnvioRequisicao = '', captchaValidationHash = '') {
        const GATEWAYS_DE_CARTAO_INTERMEDIDADORES = ['GN', 'GP'];

        const hs_web = new HotsiteWeb();
        const criar_toast = this.criarToast;

        if (paymentToken == '') {
            if (creditcardGateway == 'GN') {
                this.gerarPaymentTokenGerenciaNetCardEPagar(id_receber, parcelas, numero_cartao, mes, ano, bandeira, dv, recorrente, cartaoNome, creditCardAmbiente, gerenciaIdentificadorConta, captchaValidationHash);
                return;
            } else if (creditcardGateway == 'IG') {
                this.generateIuguPaymentToken(id_receber, parcelas, numero_cartao, mes, ano, bandeira, dv, recorrente, cartaoNome,creditCardAmbiente,gerenciaIdentificadorConta, captchaValidationHash);
                return;
            }
        }
        const data = {
            ID_RECEBER: id_receber || 0,
            PARCELAMENTO: parcelas || 0,
            NUMERO_CARTAO: numero_cartao || 0,
            MES: mes || 0,
            ANO: ano || 0,
            BANDEIRA: bandeira || '',
            DV: dv || 0,
            RECORRENTE: recorrente || 0,
            CARTAO_NOME: cartaoNome || '',
            PAYMENT_TOKEN: paymentToken,
            DATA_ENVIO_REQUISICAO: dataEnvioRequisicao,
            ACTION: 'setPagamentoCartao',
            CAPTCHA_VALIDATION_HASH: captchaValidationHash,
        };

        try {
            $.post(`${__SERVER__}/model/faturas/faturas.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    data = data;
                }

                if (data != '' && data != undefined) {
                    hs_web.esconderToast('processandoPagamento');
                    const message = data[0];
                    if (message.tipo == 'sucesso') {
                        if (GATEWAYS_DE_CARTAO_INTERMEDIDADORES.includes(creditcardGateway)) {
                            criar_toast(
                                10000,
                                message?.title ?? 'Sucesso!',
                                message.mensagem,
                                message?.icon ?? 'fas fa-check',
                                message?.color ?? 'blue',
                                'id_sucesso',
                                true,
                            );
                        } else {
                            criar_toast(3000, 'Sucesso!', 'Transação finalizada! Aguardando confirmação do pagamento. Isso poderá levar algumas horas. Por favor, aguarde',
                                'fas fa-check', 'green', 'id_sucesso', true);
                            hs_web.criarToastComprovante(id_receber, recorrente);
                        }
                        router.replace({ path: '/central_assinante_web/faturas' });
                    } else if (message.tipo == 'erro_tentativa_pagamento') {
                        hs_web.esconderToast('processandoPagamento');
                        hs_web.encerrarSessaoHotsite();

                        alert(
                            'Limite de tentativas excedido.\n'
                            + 'Por motivos de segurança, o limite máximo de tentativas de pagamento foi atingido. É necessário realizar o login novamente para continuar o processo de pagamento.',
                        );

                        window.location.reload();
                    } else {
                        const { gateway } = data[0];
                        const { errorCode, showErrors } = data[0];
                        let message;

                        const creditCardException = new CreditCardsExceptions();

                        if (gateway === 'C3' || gateway === 'YA') {
                            message = creditCardException.getMessageException(gateway, errorCode, bandeira);
                        } else if (errorCode && (creditCardException.showMessageException(gateway, errorCode) || showErrors)) {
                            message = data[0].mensagem;
                        }

                        if (!message) {
                            message = 'Erro ao realizar pagamento. Por favor, verifique as informações fornecidas para o pagamento e tente novamente.';
                        }

                        criar_toast(3000, 'Ops!', message, 'fas fa-exclamation-circle', 'red', 'id_erro', false);
                    }
                }
                app.loading = false;
            }).fail((e) => {
                hs_web.esconderToast('processandoPagamento');
                criar_toast('3000', 'Erro!', 'Ocorreu um erro no servidor. Por favor entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
                setTimeout("location.href = '/central_assinante_web/faturas';", 3000);
            });
            function noBack() { window.history.forward(); }
            noBack();
            window.onload = noBack;
            window.onpageshow = function (evt) { if (evt.persisted)noBack(); };
            window.onunload = function () { void (0); };
        } catch (e) {
            return `Exception Class HotsiteWeb -> pagarFatura() -> ${e}`;
        }
    }

    generateIuguPaymentToken(id_receber, parcelas, numero_cartao, mes, ano, bandeira, dv, recorrente, cartaoNome,creditCardAmbiente,gerenciaIdentificadorConta, captchaValidationHash = '') {
        Iugu.setAccountID(gerenciaIdentificadorConta);
        Iugu.setTestMode(creditCardAmbiente === 'T');

        const [firstName, lastName] = cartaoNome ? Iugu.utils.getFirstLastNameByFullName(cartaoNome) : ['', ''];
        const cardNumber = numero_cartao.replaceAll(/\D/g, '');

        const hs_web = new HotsiteWeb();
        const criar_toast = this.criarToast;

        if (!this.invalidIuguCardDetails(cardNumber, dv, bandeira, mes, ano, firstName, lastName)) {
            const creditCard = Iugu.CreditCard(cardNumber, mes, ano, firstName, lastName, dv);

            Iugu.createPaymentToken(creditCard, function(response) {
                if (!response.errors) {
                    const now = new Date();
                    const data = now.getFullYear()+'-'+(now.getMonth()+1).toString().padStart(2, '0')+'-'+now.getDate().toString().padStart(2, '0')+' '+now.getHours()+':'+now.getMinutes().toString().padStart(2, '0')+':'+now.getSeconds().toString().padStart(2, '0');
                    hs_web.pagarFatura(id_receber, parcelas, numero_cartao, mes, ano, bandeira, dv, recorrente, cartaoNome, creditCardAmbiente,'IG',gerenciaIdentificadorConta,response.id, data, captchaValidationHash);
                } else {
                    const message = 'Erro ao realizar pagamento. Por favor, verifique as informações fornecidas para o pagamento e tente novamente.';
                    criar_toast(3000, 'Ops!', message, 'fas fa-exclamation-circle', 'red', 'id_erro', false);
                    app.loading=false;
                }
            });
        }
    }

    invalidIuguCardDetails(cardNumber, dv, bandeira, mes, ano, firstName, lastName) {
        let invalidFields = [];
        let requiredFields = [];

        if (!cardNumber) {
            requiredFields.push('Número do cartão');
        } else if (!Iugu.utils.validateCreditCardNumber(cardNumber)) {
            invalidFields.push('Número do cartão');
        }
        if (!firstName && !lastName) {
            requiredFields.push('Nome impresso no cartão');
        } else if (!Iugu.utils.validateFirstName(firstName) || !Iugu.utils.validateLastName(lastName)) {
            invalidFields.push('Nome impresso no cartão');
        }
        if (!mes && !ano) {
            requiredFields.push('Data de validade');
        } else if (!Iugu.utils.validateExpiration(mes, ano)) {
            invalidFields.push('Data de validade');
        }
        if (!dv) {
            requiredFields.push('Código de segurança');
        } else if (!Iugu.utils.validateCVV(dv, bandeira)) {
            invalidFields.push('Código de segurança');
        }

        return this.handleInvalidIuguCard(invalidFields, requiredFields);
    }

    handleInvalidIuguCard(invalidFields, requiredFields) {
        let message = '';
        let timeout = 3000;

        if (requiredFields.length > 1) {
            timeout = 5000;
            const fields = requiredFields.join('", "');
            message = 'Notamos que os campos "' + fields + '" continuam vazios, preencha-os para prosseguir.';
        } else if (requiredFields.length === 1) {
            message = 'Quase pronto! Apenas o campo "' + requiredFields[0] + '" faltou ser preenchido. Preencha para continuar.';
        } else if (invalidFields.length > 1) {
            timeout = 5000;
            const fields = invalidFields.join('", "');
            message = 'As informações inseridas nos campos "' + fields + '", estão inválidas. Verifique e tente novamente.';
        } else if (invalidFields.length === 1) {
            message = 'A informação inserida no campo "' + invalidFields[0] + '", está inválida. Verifique e tente novamente.';
        }

        if (message) {
            this.criarToast(timeout, 'Ops!', message, 'fas fa-exclamation-circle', 'red', 'id_erro', false);
            app.loading = false;
            return true;
        }

        return false;
    }

    gerarPaymentTokenGerenciaNetCardEPagar(id_receber, parcelas, numero_cartao, mes, ano, bandeira, dv, recorrente, cartaoNome, creditCardAmbiente, gerenciaIdentificadorConta, captchaValidationHash = '') {
        const urlBase = creditCardAmbiente == 'T' ? 'https://sandbox.gerencianet.com.br/v1/cdn/' : 'https://api.gerencianet.com.br/v1/cdn/';
        const s = document.createElement('script');
        s.type = 'text/javascript';
        const v = parseInt(Math.random() * 1000000);
        s.src = `${urlBase + gerenciaIdentificadorConta}/${v}`;
        s.async = false;
        s.id = gerenciaIdentificadorConta;
        if (!document.getElementById(gerenciaIdentificadorConta)) {
            document.getElementsByTagName('head')[0].appendChild(s);
        }
        top.$gn = {
            validForm: true,
            processed: false,
            done: {},
            ready(fn) {
                $gn.done = fn;
            },
        };
        $gn.ready((checkout) => {
            top.checkout = checkout;
            this.gerarPaymentTokenGerenciaNet(id_receber, parcelas, numero_cartao, mes, ano, bandeira, dv, recorrente, cartaoNome, creditCardAmbiente, 'GN', gerenciaIdentificadorConta, captchaValidationHash);
        });

        if (typeof top.checkout !== 'undefined') {
            this.gerarPaymentTokenGerenciaNet(id_receber, parcelas, numero_cartao, mes, ano, bandeira, dv, recorrente, cartaoNome, creditCardAmbiente, 'GN', gerenciaIdentificadorConta, captchaValidationHash);
        }
    }

    gerarPaymentTokenGerenciaNet(id_receber, parcelas, numero_cartao, mes, ano, bandeira, dv, recorrente, cartaoNome, creditCardAmbiente, creditcardGateway, gerenciaIdentificadorConta, captchaValidationHash = '') {
        const criar_toast = this.criarToast;
        const hs_web = new HotsiteWeb();
        const callback = (error, response) => {
            if (error) {
                criar_toast(3000, 'Ops!', this.getErrorMessageGerencianetCard(error), 'fas fa-exclamation-circle', 'red', 'id_erro', false);
                app.loading = false;
            } else {
                const now = new Date();
                const data = `${now.getFullYear()}-${(now.getMonth() + 1).toString().padStart(2, '0')}-${now.getDate().toString().padStart(2, '0')} ${now.getHours()}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`;
                hs_web.pagarFatura(id_receber, parcelas, numero_cartao, mes, ano, bandeira, dv, recorrente, cartaoNome, creditCardAmbiente, creditcardGateway, gerenciaIdentificadorConta, response.data.payment_token, data, captchaValidationHash);
            }
        };

        top.checkout.getPaymentToken({
            brand: bandeira,
            number: numero_cartao.replace(/\D/g, ''),
            cvv: dv,
            expiration_month: mes,
            expiration_year: ano,
            reuse: recorrente,
        }, callback);
    }

    getErrorMessageGerencianetCard(error) {
        const invalidParameters = {
            invalid_brand: 'A bandeira do cartão informada é inválida. As opções válidas são: Visa, Mastercard, Amex, Diners, Elo ou Hipercard',
            invalid_card_number: 'O número do cartão informado é inválido',
            invalid_expiration_month: 'A data de validade informada é inválida',
        }

        const invalidProperties = {
            cvv: 'O Código de segurança informado é inválido',
            expiration_year: 'A data de validade informada é inválida',
        }

        if (invalidParameters[error.error]) {
            return invalidParameters[error.error];
        } else if (error.error === 'invalid_data' && invalidProperties[error.error_description?.property]) {
            return invalidProperties[error.error_description.property];
        }

        return error.error_description?.message ?? error.error_description;
    }

    comprovantePagamento(id_receber, acao) {
        const data = {
            ID_RECEBER: id_receber || 0,
            ACAO: acao,
            APP: app.cordova_app ? 'S' : 'N',
            ACTION: 'getImprinmirComprovanteCartao',
        };

        const hs_web = new HotsiteWeb();

        try {
            if (data.APP === 'S' && acao == 'N') {
                app.loading = true;

                const fileTransfer = new FileTransfer();
                const fileURL = `${cordova.file.dataDirectory}comprovante.pdf`;
                const uri = `${__SERVER__}/model/faturas/faturas.php?ID_RECEBER=${data.ID_RECEBER}&ACAO=${data.ACAO}&APP=${data.APP}&ACTION=${data.ACTION}`;

                fileTransfer.download(uri, fileURL,
                    (entry) => {
                        cordova.plugins.fileOpener2.open(entry.toURL(), 'application/pdf', {
                            error(e) {
                                app.loading = false;
                                alert('Instale um leitor de arquivo .pdf!');
                            },
                            success(e) {
                                app.loading = false;
                            },
                        });
                    },
                    (e) => {
                        app.loading = false;
                        alert('Erro ao baixar arquivo!');
                    }, false, {});
            } else {
                $.get(`${__SERVER__}/model/faturas/faturas.php`, data, (data) => {
                    try {
                        data = JSON.parse(data);
                    } catch (e) {
                        data = data;
                    }

                    if (data != '' && data != undefined) {
                        if (data.tipo == 'sucesso') {
                            if (typeof (data.mensagem) === 'object' && typeof window.ReactNativeWebView === 'undefined') {
                                app.modal_nome = `Comprovante - Fatura ${id_receber}`;
                                app.modal_url = data.mensagem.base_pdf;
                                app.loading_modal = false;

                                return $('#modalImpressao').modal('toggle');
                            }
                            postMessageMobile('base64', JSON.stringify({
                                base: data.mensagem.base_pdf,
                                nome: btoa(`${Math.floor(Math.random() * 9999) + 1000}comprovante.pdf`),
                            })).then(
                                (retorno) => {
                                    if (JSON.parse(retorno.data).retorno == 'success') {
                                        criar_toast('2000', 'Sucesso!', 'O seu download foi completado com êxito', 'fas fa-check', 'green', 'id_sucesso');
                                    } else {
                                        criar_toast('2000', 'Erro!', 'Ocorreu algum erro ao fazer o download', 'fas fa-exclamation-circle', 'red', 'id_erro');
                                    }
                                },
                            );

                            return hs_web.criarToast('2000', 'Sucesso!', data.mensagem, 'fas fa-check', 'green', 'id_sucesso');
                        }
                        return hs_web.criarToast(5000, 'Ops!', data.mensagem, 'fas fa-exclamation-circle', 'red', 'id_erro', false);
                    }
                }).fail((e) => hs_web.criarToast(5000, 'Ops!', data.mensagem, 'fas fa-exclamation-circle', 'red', 'id_erro', false));
            }
        } catch (e) {
            hs_web.criarToast(5000, 'Ops!', data.mensagem, 'fas fa-exclamation-circle', 'red', 'id_erro', false);
            return `Exception Class HotsiteWeb -> pagarFatura() -> ${e}`;
        }
    }

    recorrenciaVindi() {
        $(document).ready(() => {
            const hs_web = new HotsiteWeb();

            app.loading = false;
            router.history.current.matched[0].instances.default.loading = false;
            validaActiveClass('#pg_plano');
            hs_web.setarPermissaoFaturas();
            hs_web.instanciarCartao();

            this.loadCaptcha();
        });
    }

    buscarParcelas(id_receber) {
        const data = {
            ACTION: 'getParcelas',
            ID_RECEBER: id_receber || 0,
        };

        const criar_toast = this.criarToast;

        try {
            $.get(`${__SERVER__}/model/faturas/faturas.php`, data, (data) => {
                data = JSON.parse(data);
                if (data != '' && data != undefined) {
                    if (data[0].tipo == 'sucesso') {
                        router.history.current.matched[0].instances.default.cartao_parcelas = data[0].mensagem.parcelas;
                        router.history.current.matched[0].instances.default.permite_recorrencia = !!data[0].mensagem.recorrente.permite_recorrencia;
                        router.history.current.matched[0].instances.default.contrato_recorrente = !!data[0].mensagem.recorrente.existe_token;

                        router.history.current.matched[0].instances.default.recorrente = !!data[0].mensagem.recorrente.existe_token;
                        router.history.current.matched[0].instances.default.credit_card_gateway = data[0].mensagem.gateway;

                        if (data[0].mensagem.recorrente.existe_token) {
                            criar_toast('5000', 'Informativo!', 'Esta fatura já possui pagamento programado como recorrente!', 'fas fa-exclamation-circle', 'yellow', 'id_erro');
                        }
                    } else if (!data[0].tipo == 'erro') {
                        criar_toast('4000', 'Ops!', 'Houve algum problema ao tentar gerar as parcelas. Entre em contato conosco.', 'fas fa-exclamation-circle', 'red', 'id_erro');
                    }

                    $(document).ready(() => {
                        const hs_web = new HotsiteWeb();

                        app.loading = false;
                        router.history.current.matched[0].instances.default.loading = false;
                        validaActiveClass('#pg_fatura');
                        hs_web.setarPermissaoFaturas();
                        hs_web.instanciarCartao();

                        this.loadCaptcha();
                    });
                }
            }).fail((e) => {
                router.history.current.matched[0].instances.default.cartao_parcelas = [];
                $(document).ready(() => {
                    app.loading = false;
                    router.history.current.matched[0].instances.default.loading = false;
                    validaActiveClass('#pg_fatura');
                });
                criar_toast('5000', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Method buscarParcelas() -> ${e}`;
        }
    }

    loadCaptcha() {
        const refreshCaptcha = this.refreshCaptchaImage;

        const captchaRequestData = {
            length: 7,
            height: 40,
            width: 200,
        };

        const xhttp = new XMLHttpRequest();

        xhttp.onreadystatechange = function () {
            if (this.readyState === 4 && this.status === 200) {
                const img = document.createElement('img');
                const response = JSON.parse(xhttp.responseText);
                img.id = 'captchaPaymentImage';
                img.src = `data:image/png;base64,${response.data.captchaImageBase64}`;
                img.style.width = captchaRequestData.width;
                img.style.height = captchaRequestData.height;
                img.addEventListener('click', (event) => {
                    refreshCaptcha(event.srcElement.id);
                });
                document.getElementById('rowCaptcha').prepend(img);
                document.getElementById('captchaKey').value = response.data.cacheKey;
            }
        };

        xhttp.open('GET', '/api-module/auth/generate-captcha-image?length=7&height=40&width=200', true);
        xhttp.send();
    }

    refreshCaptchaImage(imageElementId) {
        const xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function () {
            if (this.readyState === 4 && this.status === 200) {
                const img = document.getElementById(imageElementId);
                const response = JSON.parse(xhttp.responseText);
                img.src = `data:image/png;base64,${response.data.captchaImageBase64}`;
                document.getElementById('captchaKey').value = response.data.cacheKey;
            }
        };
        xhttp.open('GET', '/api-module/auth/generate-captcha-image?length=7&height=40&width=200', true);
        xhttp.send();
    }

    buscarCarteirasFatura() {
        const data = {
            ACTION: 'getCarteirasFatura',
        };
        const criar_toast = this.criarToast;

        try {
            $.get(`${__SERVER__}/model/faturas/faturas.php`, data, (data) => {
                data = JSON.parse(data);
                if (data.utiliza_fatura && data.carteiras.length == 0) {
                    criar_toast('5000', 'Ops!', 'Não existe nenhuma carteira configurada para aparecer na central, entre em contato com o suporte', 'fas fa-exclamation-circle', 'red', 'id_erro');
                } else if (data != '' && data != undefined) {
                    $(document).ready(() => {
                        app.loading = false;
                        router.history.current.matched[0].instances.default.loading = false;
                        app.carteiras_faturas = data.carteiras;
                    });
                }
            }).fail((e) => {
                router.history.current.matched[0].instances.default.cartao_parcelas = [];
                $(document).ready(() => {
                    app.loading = false;
                    router.history.current.matched[0].instances.default.loading = false;
                    validaActiveClass('#pg_fatura');
                });
                criar_toast('5000', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Method buscarParcelas() -> ${e}`;
        }
    }

    /**
     * Notas
     */
    buscarNotas(slice) {
        const data = {
            SLICE: slice || this.slice,
            ACTION: 'getNotas',
        };

        const criar_toast = this.criarToast;
        const valida_resize_notas = this.validaResizeNotas;

        try {
            $.get(`${__SERVER__}/model/notas/notas.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    data = data;
                }

                data.total_registros = data.slice(-1)[0].total_registros;

                if (data) {
                    router.history.current.matched[0].instances.default.notas = data;
                    valida_resize_notas();

                    router.history.current.matched[0].instances.default.is_safari = IS_SAFARI;
                    router.history.current.matched[0].instances.default.mostrar_cinco_notas = data.total_registros <= SLICE_NOTA;
                    router.history.current.matched[0].instances.default.carregar_mais_notas = !router.history.current.matched[0].instances.default.mostrar_cinco_notas;

                    $(document).ready(() => {
                        app.loading = false;
                        router.history.current.matched[0].instances.default.loading = false;
                        validaActiveClass('#pg_nota');
                    });

                    return;
                }

                $(document).ready(() => {
                    app.loading = false;
                    router.history.current.matched[0].instances.default.loading = false;
                    validaActiveClass('#pg_nota');
                });

                router.history.current.matched[0].instances.default.notas = [];
            }).fail((e) => {
                router.history.current.matched[0].instances.default.notas = [];
                router.history.current.matched[0].instances.default.mostrar_cinco_notas = false;
                $(document).ready(() => {
                    app.loading = false;
                    router.history.current.matched[0].instances.default.loading = false;
                    validaActiveClass('#pg_nota');
                });
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> buscarNotas() -> ${e}`;
        }
    }

    imprimirNota(id_saida) {
        this.restoreModalVariavel();

        const data = {
            ID_SAIDA: id_saida || 0,
            APP: app.cordova_app ? 'S' : 'N',
            ACTION: 'getImprimirNota',
        };

        const criar_toast = this.criarToast;
        app.loading_modal = true;

        try {
            if (data.APP === 'S') {
                app.loading = true;

                const fileTransfer = new FileTransfer();
                const fileURL = `${cordova.file.dataDirectory}notas.pdf`;
                const uri = `${__SERVER__}/model/notas/notas.php?ID_SAIDA=${data.ID_SAIDA}&APP=${data.APP}&ACTION=${data.ACTION}`;

                fileTransfer.download(uri, fileURL,
                    (entry) => {
                        cordova.plugins.fileOpener2.open(entry.toURL(), 'application/pdf', {
                            error(e) {
                                app.loading = false;
                                alert('Instale um leitor de arquivo .pdf!');
                            },
                            success(e) {
                                app.loading = false;
                            },
                        });
                    },
                    (e) => {
                        app.loading = false;
                        alert('Erro ao baixar arquivo!');
                    }, false, {});
            } else {
                $.get(`${__SERVER__}/model/notas/notas.php`, data, (data) => {
                    if (typeof window.ReactNativeWebView !== 'undefined') {
                        data = JSON.parse(data);
                        $('#btn_imprimir_fat, #btn_imprimir_fat_, #btn_imprimir_fat_home, #btn_imprimir_fat_home_').attr('disabled', false);
                        document.getElementById('modalImpressaoClose').click();
                        postMessageMobile('base64', JSON.stringify({
                            base: data[0].mensagem.base_pdf,
                            nome: btoa((Math.floor(Math.random() * 9999) + 1000)) + data[1],
                        })).then(
                            (retorno) => {
                                $('#btn_imprimir_fat_').attr('disabled', false);
                                if (JSON.parse(retorno.data).retorno == 'success') {
                                    criar_toast('2000', 'Sucesso!', 'O seu download foi completado com êxito', 'fas fa-check', 'green', 'id_sucesso');
                                } else {
                                    criar_toast('2000', 'Erro!', 'Ocorreu algum erro ao fazer o download', 'fas fa-exclamation-circle', 'red', 'id_erro');
                                }
                            },
                        );
                    } else {
                        if (data != '<strong>TCPDF ERROR: </strong>[Image] Unable to get the size of the image: /var/www/includes/nfe/images/logo.jpg') {
                            data = JSON.parse(data);
                        }

                        if (data[2] == 'true') {
                            const xmlHttp = new XMLHttpRequest();
                            xmlHttp.open('GET', `${__SERVER__}/model/notas/notas.php?APP=N&ACTION=retornarArquivo&CAMINHO=${data[1]}`, true);
                            xmlHttp.responseType = 'blob';
                            xmlHttp.onreadystatechange = function (e) {
                                if (xmlHttp.readyState === 4 && (xmlHttp.status === 200 || xmlHttp.status === 0)) {
                                    const link = document.createElement('a');
                                    link.href = URL.createObjectURL(xmlHttp.response);
                                    link.download = 'Nota.pdf';
                                    link.click();
                                    document.getElementById('modalImpressaoClose').click();
                                }
                            };
                            xmlHttp.send();
                        }

                        app.loading_modal = false;
                        if (data != '' && data != undefined && !navigator.userAgent.match(/Android|BlackBerry|iPhone|iPad|iPod|IEMobile/i)) {
                            if (data[0].tipo == 'sucesso') {
                                return app.modal_url = data[0].mensagem.base_pdf;
                            }
                            criar_toast('2000', 'Erro!', 'Ocorreu um erro ao imprimir a nota.', 'fas fa-exclamation-circle', 'red', 'id_erro');
                        }
                    }
                }).fail((e) => {
                    app.modal_url = '';
                    app.loading_modal = false;
                    criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
                });
            }
        } catch (e) {
            return `Exception Class HotsiteWeb -> imprimirNota() -> ${e}`;
        }
    }

    /**
     * Contratos
     */
    buscarPlanos(slice, home, criar_toast_pendencia, pg_planos, valida_loading, valida_resize_home, valida_resize_planos, valida_loading_function) {
        const data = {
            SLICE: slice || this.slice,
            HOME: home || false,
            ACTION: 'getPlanos',
        };

        var criar_toast_pendencia = criar_toast_pendencia || this.criarToastPendencia;
        var valida_resize_home = valida_resize_home || this.validaResizeHome;
        var valida_resize_planos = valida_resize_planos || this.validaResizePlanos;
        var valida_loading_function = valida_loading_function || this.validaLoading;

        try {
            $.get(`${__SERVER__}/model/planos/planos.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    data = data;
                }

                if (data != '' && data != undefined) {
                    if (data.num_contratos_dash > 0 && app.pg_plano === 'S') {
                        if (home == true) {
                            const contratoSuspenso = data.planos[0].contrato_suspenso;
                            const statusVelocidade = data.planos[0].status_velocidade ? data.planos[0].status_velocidade : null;
                            const statusInternet = data.planos[0].status_internet ? data.planos[0].status_internet : null;
                            const desbloqueioConfianca = data.planos[0].desbloqueio_confianca ? data.planos[0].desbloqueio_confianca : null;

                            if (desbloqueioConfianca === 'S' && (statusInternet === 'CA' || statusInternet === 'CM') && contratoSuspenso === 'N') {
                                criar_toast_pendencia('12000', 'Opa!', 'Desculpe, sua internet está temporariamente bloqueada pois existem mensalidades em atraso. Para usar a internet novamente, solicite o desbloqueio de confiança ou faça você mesmo pela central do assinante, na parte de contratos. Caso já tenha efetuado o pagamento, entre em contato conosco.', 'receipt', 'red', 'notificacao_planos', 'Ver contrato', 'planos');
                            } else if (statusInternet === 'FA' && statusVelocidade === 'R' && contratoSuspenso === 'N') {
                                criar_toast_pendencia('12000', 'Opa!', 'Seu contrato está com redução de velocidade devido a pendências financeiras. Por favor regularize sua situação financeira o quanto antes e aproveite a melhor internet!', 'receipt', 'red', 'notificacao_planos', 'Ver contrato', 'planos');
                            } else if (contratoSuspenso !== 'S') {
                                criar_toast_pendencia('12000', 'Opa!', 'Parece que você tem pendências nos contratos!', 'receipt', 'red', 'notificacao_planos', 'Ver contrato', 'planos');
                            }
                        }
                    }

                    data.contratos_dash = data.contratos_dash.filter((contrato) => contrato.data_assinatura === '0000-00-00');
                    router.history.current.matched[0].instances.default.planos = data;
                    router.history.current.matched[0].instances.default.is_safari = IS_SAFARI;

                    if (data.total_registros <= SLICE_PLANO) {
                        router.history.current.matched[0].instances.default.mostrar_cinco_planos = true;
                    } else {
                        router.history.current.matched[0].instances.default.mostrar_cinco_planos = false;
                    }

                    if (pg_planos) {
                        valida_resize_planos();
                    } else {
                        valida_resize_home();
                    }

                    $(document).ready(() => {
                        if (valida_loading) {
                            LOADING_PLANO = false;
                            valida_loading_function();
                        } else {
                            app.loading = false;
                            router.history.current.matched[0].instances.default.loading = false;
                            validaActiveClass('#pg_plano');
                        }
                    });
                    return;
                }

                router.history.current.matched[0].instances.default.planos = {
                    planos: [],
                    pre_contratos: [],
                    contratos_dash: [],
                    financeiro_atrasado: [],
                    ativo_bloqueado: [],
                    ativo_bloqueado_desbloqueio_n: [],
                    ativo_desbloqueado: [],
                    ativo: [],
                    outros_status: [],
                };

                $(document).ready(() => {
                    if (valida_loading) {
                        LOADING_PLANO = false;
                        valida_loading_function();
                    } else {
                        app.loading = false;
                        router.history.current.matched[0].instances.default.loading = false;
                        validaActiveClass('#pg_plano');
                    }
                });
            }).fail((e) => {
                router.history.current.matched[0].instances.default.planos = [];

                $(document).ready(() => {
                    if (valida_loading) {
                        LOADING_PLANO = false;
                        valida_loading_function();
                    } else {
                        app.loading = false;
                        router.history.current.matched[0].instances.default.loading = false;
                        validaActiveClass('#pg_plano');
                    }
                });
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> buscarPlanos() -> ${e}`;
        }
    }

    desbloqueioConfianca(id_contrato, valida_loading) {
        const data = {
            ID_CONTRATO: id_contrato || 0,
            ACTION: 'setDesbloqueioConfianca',
        };
        app.loading = true;
        const criar_toast = this.criarToast;
        const buscar_planos = this.buscarPlanos;
        const valida_resize_home = this.validaResizeHome;
        const valida_resize_planos = this.validaResizePlanos;
        const valida_loading_function = this.validaLoading;

        try {
            $.get(`${__SERVER__}/model/planos/planos.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    data = data;
                }

                if (data != '' && data != undefined) {
                    if (data[0].tipo == 'sucesso') {
                        criar_toast('5000', 'Sucesso!', data[0].mensagem, 'fas fa-check', 'green', 'id_sucesso');
                    } else {
                        criar_toast('2000', 'Erro!', `Ocorreu um erro ao desbloquear o plano :${data[0].mensagem}`, 'fas fa-exclamation-circle', 'red', 'id_erro');
                    }

                    if (valida_loading) {
                        return buscar_planos(2000000000, false, criar_toast, true, valida_loading, valida_resize_home, valida_resize_planos, valida_loading_function);
                    }
                    return buscar_planos(SLICE_PLANO, false, criar_toast, true, valida_loading, valida_resize_home, valida_resize_planos, valida_loading_function);
                }
            }).fail((e) => {
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');

                if (valida_loading) {
                    return buscar_planos(2000000000, false, criar_toast, true, valida_loading, valida_resize_home, valida_resize_planos, valida_loading_function);
                }
                return buscar_planos(SLICE_PLANO, false, criar_toast, true, valida_loading, valida_resize_home, valida_resize_planos, valida_loading_function);
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> desbloqueioConfianca() -> ${e}`;
        }
    }

    liberacaoSuspensaoParcial(id_contrato, valida_loading) {
        const data = {
            ID_CONTRATO: id_contrato || 0,
            ACTION: 'setLiberacaoSuspensaoParcial',
        };
        app.loading = true;
        const criar_toast = this.criarToast;
        const buscar_planos = this.buscarPlanos;
        const valida_resize_home = this.validaResizeHome;
        const valida_resize_planos = this.validaResizePlanos;
        const valida_loading_function = this.validaLoading;

        try {
            $.get(`${__SERVER__}/model/planos/planos.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    data = data;
                }

                if (data != '' && data != undefined) {
                    if (data[0].type == 'success') {
                        criar_toast('5000', 'Sucesso!', data[0].mensagem, 'fas fa-check', 'green', 'id_sucesso');
                    } else {
                        criar_toast('2000', 'Erro!', `Ocorreu um erro ao liberar o plano :${data[0].mensagem}`, 'fas fa-exclamation-circle', 'red', 'id_erro');
                    }

                    if (valida_loading) {
                        return buscar_planos(2000000000, false, criar_toast, true, valida_loading, valida_resize_home, valida_resize_planos, valida_loading_function);
                    }
                    return buscar_planos(SLICE_PLANO, false, criar_toast, true, valida_loading, valida_resize_home, valida_resize_planos, valida_loading_function);
                }

                if (!data.length) {
                    criar_toast('2000', 'Erro!', 'Ocorreu um erro ao liberar o plano. Por favor, tente novamente ou entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
                    app.loading = false;
                }
            }).fail((e) => {
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');

                if (valida_loading) {
                    return buscar_planos(2000000000, false, criar_toast, true, valida_loading, valida_resize_home, valida_resize_planos, valida_loading_function);
                }
                return buscar_planos(SLICE_PLANO, false, criar_toast, true, valida_loading, valida_resize_home, valida_resize_planos, valida_loading_function);
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> liberacaoSuspensaoParcial() -> ${e}`;
        }
    }

    salvarAssinatura(id_contrato, assinatura_base, valida_loading) {
        const data = {
            ID_CONTRATO: id_contrato || 0,
            ASSINATURA_BASE: assinatura_base || '',
            ACTION: 'setSalvarAssinatura',
        };

        PAGE_PLANOS = !!valida_loading;
        app.loading = true;

        const criar_toast = this.criarToast;
        const buscar_planos = this.buscarPlanos;
        const valida_resize_home = this.validaResizeHome;
        const valida_resize_planos = this.validaResizePlanos;
        const valida_loading_function = this.validaLoading;

        try {
            $.post(`${__SERVER__}/model/planos/planos.php`, data, (data) => {
                if (data != '<strong>TCPDF ERROR: </strong>Please provide a certificate file and password!') {
                    data = JSON.parse(data);
                }

                if (data != '' && data != undefined) {
                    if (data[0].tipo == 'sucesso') {
                        app.loading = false;
                        criar_toast('4000', 'Sucesso!', 'O contrato foi assinado com sucesso.', 'fas fa-check', 'green', 'id_sucesso');
                    } else {
                        app.loading = false;
                        criar_toast('4000', 'Erro!', data[0].mensagem, 'fas fa-exclamation-circle', 'red', 'id_erro');
                    }
                    if (valida_loading) {
                        return buscar_planos(2000000000, false, criar_toast, false, valida_loading, valida_resize_home, valida_resize_planos, valida_loading_function);
                    }
                    return buscar_planos(SLICE_PLANO, false, criar_toast, true, valida_loading, valida_resize_home, valida_resize_planos, valida_loading_function);
                }
            }).fail((e) => {
                if (valida_loading) {
                    return buscar_planos(2000000000, false, criar_toast, false, valida_loading, valida_resize_home, valida_resize_planos, valida_loading_function);
                }
                return buscar_planos(SLICE_PLANO, false, criar_toast, true, valida_loading, valida_resize_home, valida_resize_planos, valida_loading_function);

                app.loading = false;
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> desbloqueioConfianca() -> ${e}`;
        }
    }

    salvarAssinaturaTermo(id_contrato, assinatura_base, valida_loading, id_termo, cliente_contrato_assinatura_termo_id) {
        const data = {
            ID_CONTRATO: id_contrato || 0,
            ASSINATURA_BASE: assinatura_base || '',
            ACTION: 'setSalvarAssinaturaTermo',
            ID_TERMO: id_termo,
            CLIENTE_CONTRATO_ASSINATURA_TERMO_ID: cliente_contrato_assinatura_termo_id,
        };

        let disparaAjax = false;

        PAGE_PLANOS = !!valida_loading;
        app.loading = true;

        const criar_toast = this.criarToast;
        const buscar_planos = this.buscarPlanos;
        const valida_resize_home = this.validaResizeHome;
        const valida_resize_planos = this.validaResizePlanos;
        const valida_loading_function = this.validaLoading;

        const assinatura = data.ASSINATURA_BASE;

        if (assinatura.length > 1800) {
            data.ASSINATURA_BASE = assinatura;
            disparaAjax = true;
        }

        try {
            if (disparaAjax) {
                $.ajax({
                    url: `${__SERVER__}/model/planos/planos.php`,
                    type: 'POST',
                    data,
                }).done((data) => {
                    if (data != '<strong>TCPDF ERROR: </strong>Please provide a certificate file and password!') {
                        data = JSON.parse(data);
                    }

                    if (data != '' && data != undefined && data != null) {
                        if (data[0].tipo == 'sucesso') {
                            app.loading = false;
                            router.history.current.matched[0].instances.default.assinatura_termo = false;
                            criar_toast('4000', 'Sucesso!', data[0].mensagem, 'fas fa-check', 'green', 'id_sucesso');
                        } else {
                            app.loading = false;
                            criar_toast('4000', 'Erro!', data[0].mensagem, 'fas fa-exclamation-circle', 'red', 'id_erro');
                        }

                        if (valida_loading) {
                            return buscar_planos(2000000000, false, criar_toast, false, valida_loading, valida_resize_home, valida_resize_planos, valida_loading_function);
                        }
                        return buscar_planos(SLICE_PLANO, false, criar_toast, true, valida_loading, valida_resize_home, valida_resize_planos, valida_loading_function);
                    }
                }).fail(() => {
                    if (valida_loading) {
                        return buscar_planos(2000000000, false, criar_toast, false, valida_loading, valida_resize_home, valida_resize_planos, valida_loading_function);
                    }
                    return buscar_planos(SLICE_PLANO, false, criar_toast, true, valida_loading, valida_resize_home, valida_resize_planos, valida_loading_function);

                    app.loading = false;
                    criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
                    $('#btn_enviar_assinatura').prop('disabled', false);
                });
            }
        } catch (e) {
            return `Exception --> ${e}`;
        }
    }

    imprimirPlano(id_contrato, id_termo = 0, cliente_contrato_assinatura_termo_id = 0) {
        this.restoreModalVariavel();

        const data = {
            ID_CONTRATO: id_contrato || 0,
            APP: app.cordova_app ? 'S' : 'N',
            ACTION: 'getImprimirPlano',
            ID_TERMO: id_termo || null,
            CLIENTE_CONTRATO_ASSINATURA_TERMO_ID: cliente_contrato_assinatura_termo_id || null,
        };

        const criar_toast = this.criarToast;
        app.loading_modal = true;

        try {
            if (data.APP === 'S') {
                app.loading = true;

                const fileTransfer = new FileTransfer();
                const fileURL = `${cordova.file.dataDirectory}contrato.pdf`;
                const uri = `${__SERVER__}/model/planos/planos.php?ID_CONTRATO=${data.ID_CONTRATO}&APP=${data.APP}&ACTION=${data.ACTION}&ID_TERMO=${data.ID_TERMO}&CLIENTE_CONTRATO_ASSINATURA_TERMO_ID=${data.CLIENTE_CONTRATO_ASSINATURA_TERMO_ID}`;

                fileTransfer.download(uri, fileURL,
                    (entry) => {
                        cordova.plugins.fileOpener2.open(entry.toURL(), 'application/pdf', {
                            error(e) {
                                app.loading = false;
                                alert('Instale um leitor de arquivo .pdf!');
                            },
                            success(e) {
                                app.loading = false;
                                // alert('Abriu certo!');
                            },
                        });
                    },
                    (e) => {
                        app.loading = false;
                        alert('Erro ao baixar arquivo!');
                    }, false, {});
            } else {
                $.get(`${__SERVER__}/model/planos/planos.php`, data, (data) => {
                    try {
                        data = JSON.parse(data);
                    } catch (e) {
                        data = data;
                    }
                    if (typeof window.ReactNativeWebView !== 'undefined') {
                        $('#btn_imprimir_fat, #btn_imprimir_fat_, #btn_imprimir_fat_home, #btn_imprimir_fat_home_').attr('disabled', false);
                        document.getElementById('modalImpressaoClose').click();
                        postMessageMobile('base64', JSON.stringify({
                            base: data[0].mensagem.base_pdf,
                            nome: btoa(`${Math.floor(Math.random() * 9999) + 1000}arquivo.pdf`),
                        })).then(
                            (retorno) => {
                                $('#btn_imprimir_fat_').attr('disabled', false);
                                if (JSON.parse(retorno.data).retorno == 'success') {
                                    criar_toast('2000', 'Sucesso!', 'O seu download foi completado com êxito', 'fas fa-check', 'green', 'id_sucesso');
                                } else {
                                    criar_toast('2000', 'Erro!', 'Ocorreu algum erro ao fazer o download', 'fas fa-exclamation-circle', 'red', 'id_erro');
                                }
                            },
                        );
                    } else {
                        if (data[2] == 'true') {
                            const xmlHttp = new XMLHttpRequest();
                            xmlHttp.open('GET', `${__SERVER__}/model/planos/planos.php?APP=N&ACTION=retornarArquivo&CAMINHO=${data[1]}`, true);
                            xmlHttp.responseType = 'blob';
                            xmlHttp.onreadystatechange = function (e) {
                                if (xmlHttp.readyState === 4 && (xmlHttp.status === 200 || xmlHttp.status === 0)) {
                                    const link = document.createElement('a');
                                    link.href = URL.createObjectURL(xmlHttp.response);
                                    link.download = 'ContratoInternet.pdf';
                                    link.click();

                                    document.getElementById('modalImpressaoClose').click();
                                }
                            };
                            xmlHttp.send();
                        }

                        if (data != '' && data != undefined) {
                            app.loading_modal = false;
                            if (data[0].tipo == 'sucesso') {
                                if (!navigator.userAgent.match(/Android|BlackBerry|iPhone|iPad|iPod|IEMobile/i)) {
                                    return app.modal_url = data[0].mensagem.base_pdf;
                                }
                            } else {
                                criar_toast('2000', 'Erro!', 'Ocorreu um erro ao imprimir o contrato.', 'fas fa-exclamation-circle', 'red', 'id_erro');
                            }
                        }
                    }
                }).fail((e) => {
                    app.loading_modal = false;
                    criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
                });
            }
        } catch (e) {
            return `Exception Class HotsiteWeb -> imprimirPlano() -> ${e}`;
        }
    }

    /**
     * Consumos
     */
    buscarConsumos(valida_loading) {
        const data = {
            ACTION: 'getConsumo',
        };

        const criar_toast = this.criarToast;
        const valida_loading_function = this.validaLoading;

        try {
            $.get(`${__SERVER__}/model/consumos/consumos.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    data = data;
                }

                if (data != '' && data != undefined && !data[0]) {
                    router.history.current.matched[0].instances.default.consumos = data;

                    if (data.consumo_diario.length && data.consumo_ultimo_mes.length && data.consumo_ultima_semana.length) {
                        router.history.current.matched[0].instances.default.selected_diario = data.consumo_diario[0].login;
                        router.history.current.matched[0].instances.default.selected_semanal = data.consumo_ultima_semana[0].login;
                        router.history.current.matched[0].instances.default.selected_mensal = data.consumo_mensal[0].login;
                    }

                    $(document).ready(() => {
                        if (valida_loading) {
                            LOADING_CONSUMO = false;
                            valida_loading_function();
                        } else {
                            app.loading = false;
                            router.history.current.matched[0].instances.default.loading = false;
                            validaActiveClass('#pg_consumo');
                        }
                    });

                    return;
                }

                $(document).ready(() => {
                    if (valida_loading) {
                        LOADING_CONSUMO = false;
                        valida_loading_function();
                    } else {
                        app.loading = false;
                        router.history.current.matched[0].instances.default.loading = false;
                        validaActiveClass('#pg_consumo');
                    }
                });

                router.history.current.matched[0].instances.default.consumos = {
                    consumo_mensal: [],
                    consumo_diario: [],
                    consumo_ultima_semana: [],
                    consumo_ultimo_mes: [],
                };
            }).fail((e) => {
                router.history.current.matched[0].instances.default.consumos = [];
                $(document).ready(() => {
                    app.loading = false;
                    router.history.current.matched[0].instances.default.loading = false;
                    validaActiveClass('#pg_consumo');
                });
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> buscarConsumos() -> ${e}`;
        }
    }

    graficoConsumo(index, consumos_download, consumos_upload, labels, x_label) {
        $(document).ready(() => {
            const grafico_consumo = document.getElementById(index).getContext('2d');
            consumos_download = consumos_download.map((item) => item.replace(',', ''));
            consumos_upload = consumos_upload.map((item) => item.replace(',', ''));

            const graficoConsumo = new Chart(grafico_consumo, {
                type: 'line',
                data: {
                    labels,
                    datasets: [{
                        label: 'Download em GB',
                        data: consumos_download,
                        borderColor: '#2bcbba',
                        backgroundColor: $(window).width() >= 460 ? '#2bcbba4a' : 'transparent',
                    }, {
                        label: 'Upload em GB',
                        data: consumos_upload,
                        borderColor: '#1E90FF',
                        backgroundColor: $(window).width() >= 460 ? '#1E90FF4f' : 'transparent',
                    }],
                },
                options: {
                    scales: {
                        yAxes: [{
                            scaleLabel: {
                                display: true,
                                labelString: 'Consumo em GB',
                            },
                        }],
                        xAxes: [{
                            gridLines: {
                                display: false,
                            },
                            scaleLabel: {
                                display: true,
                                labelString: x_label,
                            },
                        }],
                    },
                    responsive: true,
                    maintainAspectRatio: false,
                },
            });
        });
    }

    /**
     * Atendimentos
     */
    buscarAtendimentos(slice, home, filtros, criar_toast_pendencia_atendimento, pg_atendimentos, valida_loading, valida_resize_home, valida_resize_atendimentos, valida_loading_function, validar_mensagem_lida) {
        const data = {
            SLICE: slice || this.slice,
            FILTROS: filtros || [],
            HOME: home || false,
            ACTION: 'getAtendimentos',
        };

        var criar_toast_pendencia_atendimento = criar_toast_pendencia_atendimento || this.criarToastPendenciaAtendimento;
        var valida_resize_home = valida_resize_home || this.validaResizeHome;
        var valida_resize_atendimentos = valida_resize_atendimentos || this.validaResizeAtendimentos;
        var valida_loading_function = valida_loading_function || this.validaLoading;
        var validar_mensagem_lida = validar_mensagem_lida || this.validarMensagemLida;

        try {
            $.get(`${__SERVER__}/model/atendimentos/atendimentos.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    data = data;
                }

                if (data != '' && data != undefined) {
                    if (data.num_atendimentos > 0 && app.pg_atendimento === 'S') {
                        if (home == true) {
                            const id_atendimento_toast = data.atendimentos_dash[0].id;
                            criar_toast_pendencia_atendimento('40000000', 'Opa!', 'Você tem uma nova mensagem de atendimento!', 'headset_mic', 'green', 'notificacao_atendimento', 'Ver mensagem', `/central_assinante_web/atendimentos/mensagens/${id_atendimento_toast}`, id_atendimento_toast, validar_mensagem_lida);
                        }
                    }

                    router.history.current.matched[0].instances.default.is_safari = IS_SAFARI;

                    if (data.atendimentos) {
                        router.history.current.matched[0].instances.default.atendimentos = data;
                    }

                    if (data.total_registros <= SLICE_ATENDIMENTO) {
                        router.history.current.matched[0].instances.default.mostrar_cinco_atendimentos = true;
                    } else {
                        router.history.current.matched[0].instances.default.mostrar_cinco_atendimentos = false;
                    }

                    if (pg_atendimentos) {
                        valida_resize_atendimentos();
                    } else {
                        valida_resize_home();
                    }

                    $(document).ready(() => {
                        if (valida_loading) {
                            LOADING_ATENDIMENTO = false;
                            valida_loading_function();
                        } else {
                            app.loading = false;
                            router.history.current.matched[0].instances.default.loading = false;
                            validaActiveClass('#pg_atendimento');
                        }
                    });

                    return;
                }

                router.history.current.matched[0].instances.default.atendimentos = [];

                $(document).ready(() => {
                    if (valida_loading) {
                        LOADING_ATENDIMENTO = false;
                        valida_loading_function();
                    } else {
                        app.loading = false;
                        router.history.current.matched[0].instances.default.loading = false;
                    }
                });
            }).fail((e) => {
                router.history.current.matched[0].instances.default.atendimentos = [];

                $(document).ready(() => {
                    if (valida_loading) {
                        LOADING_ATENDIMENTO = false;
                        valida_loading_function();
                    } else {
                        app.loading = false;
                        router.history.current.matched[0].instances.default.loading = false;
                    }
                });
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> buscarAtendimentos() -> ${e}`;
        }
    }

    buscarMensagensAtendimento(id_atendimento, criar_toast) {
        const data = {
            ID_ATENDIMENTO: id_atendimento || 0,
            ACTION: 'getMensagens',
        };

        var criar_toast = criar_toast || this.criarToast;

        try {
            $.get(`${__SERVER__}/model/atendimentos/atendimentos.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    data = data;
                }

                if (data.tipo == 'erro') {
                    router.replace('/central_assinante_web/');
                }

                if (typeof data.atendimento !== 'undefined') {
                    if (data.atendimento != '') {
                        router.history.current.matched[0].instances.default.atendimentos = data;

                        $(document).ready(() => {
                            app.loading = false;
                            router.history.current.matched[0].instances.default.enviar_mensagem = false;
                            router.history.current.matched[0].instances.default.loading = false;
                            validaActiveClass('#pg_atendimento');
                        });
                        return;
                    }
                }

                $(document).ready(() => {
                    router.history.current.matched[0].instances.default.atendimentos = {};
                    app.loading = false;
                    router.history.current.matched[0].instances.default.enviar_mensagem = false;
                    router.history.current.matched[0].instances.default.loading = false;
                    router.replace('/central_assinante_web/');
                });
            }).fail((e) => {
                router.history.current.matched[0].instances.default.atendimentos = [];

                $(document).ready(() => {
                    app.loading = false;
                    router.history.current.matched[0].instances.default.enviar_mensagem = false;
                    router.history.current.matched[0].instances.default.loading = false;
                    validaActiveClass('#pg_atendimento');
                });
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> buscarMensagensAtendimento() -> ${e}`;
        }
    }

    enviarMensagem(id_atendimento, mensagem) {
        const data = {
            ID_ATENDIMENTO: id_atendimento || 0,
            MENSAGEM: mensagem || '',
            ACTION: 'setMensagem',
        };

        const criar_toast = this.criarToast;
        const buscar_mensagens_atendimento = this.buscarMensagensAtendimento;

        try {
            $.get(`${__SERVER__}/model/atendimentos/atendimentos.php`, data, (data) => {
                data = JSON.parse(data);

                if (data != '' && data != undefined) {
                    if (data[0].tipo == 'sucesso') {
                        criar_toast('2000', 'Sucesso!', data[0].mensagem, 'fas fa-check', 'green', 'id_sucesso');
                        router.history.current.matched[0].instances.default.nova_mensagem = '';
                    } else {
                        criar_toast('4000', 'Erro!', data[0].mensagem, 'fas fa-exclamation-circle', 'red', 'id_erro');
                    }

                    return buscar_mensagens_atendimento(id_atendimento, criar_toast);
                }
            }).fail((e) => {
                buscar_mensagens_atendimento(id_atendimento, criar_toast);
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> enviarMensagem() -> ${e}`;
        }
    }

    validarMensagemLida(id_atendimento) {
        const data = {
            ID_ATENDIMENTO: id_atendimento || 0,
            ACTION: 'setMensagemVisualizadaCliente',
        };

        try {
            $.get(`${__SERVER__}/model/atendimentos/atendimentos.php`, data, (data) => {
                data = JSON.parse(data);

                if (data != '' && data != undefined) {
                    if (data[0].tipo == 'erro') {
                        console.log(`erro: ${data[0].mensagem}`);
                    }
                }
            }).fail((e) => {
                console.log(`Ocorreu algum erro no servidor -> STATUS: ${e.statusText}`);
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> validarMensagemLida() -> ${e}`;
        }
    }

    buscarProtocolo() {
        const data = {
            ACTION: 'getProtocoloAtendimento',
        };

        const criar_toast = this.criarToast;

        try {
            $.get(`${__SERVER__}/model/atendimentos/atendimentos.php`, data, (data) => {
                data = JSON.parse(data);

                if (data != '' && data != undefined) {
                    if (!data[0].tipo) {
                        router.history.current.matched[0].instances.default.atendimentos.protocolo_novo_atendimento = data;
                    } else {
                        criar_toast('2000', 'Erro!', 'Houve algum problema ao tentar gerar o protocolo de atendimento', 'fas fa-exclamation-circle', 'red', 'id_erro');
                    }

                    $(document).ready(() => {
                        app.loading = false;
                        router.history.current.matched[0].instances.default.loading = false;
                        validaActiveClass('#pg_atendimento');
                    });
                }
            }).fail((e) => {
                router.history.current.matched[0].instances.default.atendimentos.protocolo_novo_atendimento = '';
                $(document).ready(() => {
                    app.loading = false;
                    router.history.current.matched[0].instances.default.loading = false;
                    validaActiveClass('#pg_atendimento');
                });
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Method buscarProtocolo() -> ${e}`;
        }
    }

    buscarDepartamento() {
        const data = {
            ACTION: 'getDepartamentos',
        };

        const criar_toast = this.criarToast;

        try {
            $.get(`${__SERVER__}/model/atendimentos/atendimentos.php`, data, (data) => {
                data = JSON.parse(data);

                if (data != '' && data != undefined) {
                    if (!data[0].tipo) {
                        router.history.current.matched[0].instances.default.atendimentos.departamento_novo_atendimento = data;
                    } else {
                        criar_toast('2000', 'Erro!', 'Houve algum problema ao tentar listar os departamentos', 'fas fa-exclamation-circle', 'red', 'id_erro');
                    }

                    $(document).ready(() => {
                        app.loading = false;
                        router.history.current.matched[0].instances.default.loading = false;
                        validaActiveClass('#pg_atendimento');
                    });
                }
            }).fail((e) => {
                router.history.current.matched[0].instances.default.atendimentos.departamento_novo_atendimento = [];
                $(document).ready(() => {
                    app.loading = false;
                    router.history.current.matched[0].instances.default.loading = false;
                    validaActiveClass('#pg_atendimento');
                });
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Method buscarDepartamento() -> ${e}`;
        }
    }

    buscarAssunto() {
        const data = {
            ACTION: 'getAssuntos',
        };

        const criar_toast = this.criarToast;

        try {
            $.get(`${__SERVER__}/model/atendimentos/atendimentos.php`, data, (data) => {
                data = JSON.parse(data);

                if (data != '' && data != undefined) {
                    if (data.length > 0) {
                        router.history.current.matched[0].instances.default.atendimentos.assunto_novo_atendimento = data;
                    } else {
                        criar_toast('2000', 'Erro!', 'Houve algum problema ao tentar listar os assuntos', 'fas fa-exclamation-circle', 'red', 'id_erro');
                    }

                    $(document).ready(() => {
                        app.loading = false;
                        router.history.current.matched[0].instances.default.loading = false;
                        validaActiveClass('#pg_atendimento');
                    });
                }
            }).fail((e) => {
                router.history.current.matched[0].instances.default.atendimentos.assunto_novo_atendimento = [];
                $(document).ready(() => {
                    app.loading = false;
                    router.history.current.matched[0].instances.default.loading = false;
                    validaActiveClass('#pg_atendimento');
                });
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Method buscarAssunto() -> ${e}`;
        }
    }

    enviarNovoAtendimento(titulo, prioridade, id_ticket_setor, mensagem, status, assunto, protocolo, filtros) {
        const data = {
            TITULO: titulo || '',
            PRIORIDADE: prioridade || '',
            ID_TICKET_SETOR: id_ticket_setor || 0,
            MENSAGEM: mensagem || '',
            STATUS: status || '',
            ASSUNTO: assunto || '',
            PROTOCOLO: protocolo || '',
            FILTROS: filtros || [],
            ACTION: 'setNovoAtendimento',
        };

        const criar_toast = this.criarToast;

        try {
            $.get(`${__SERVER__}/model/atendimentos/atendimentos.php`, data, (data) => {
                data = JSON.parse(data);

                if (data != '' && data != undefined) {
                    if (data[0].tipo == 'sucesso') {
                        criar_toast('3000', 'Sucesso!', 'Seu atendimento foi aberto. Responderemos dentro de algumas horas &#x1F609', 'fas fa-check', 'green', 'id_sucesso');
                        router.replace({ path: '/central_assinante_web/atendimentos' });
                    } else {
                        criar_toast('5000', 'Erro!', 'Não foi possível abrir o atendimento. Verifique se todos os campos estão preenchidos.', 'fas fa-exclamation-circle', 'red', 'id_erro');
                    }
                }
            }).fail((e) => {
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> enviarNovoAtendimento() -> ${e}`;
        }
    }

    finalizarAtendimento(id_atendimento, filtros, slice, mensagem_finalizacao, criar_toast, buscar_atendimentos, valida_resize_home, valida_resize_atendimentos, valida_loading_function, validar_mensagem_lida) {
        const data = {
            ID_ATENDIMENTO: id_atendimento || 0,
            MENSAGEM: mensagem_finalizacao || '',
            SLICE: slice || this.slice,
            FILTROS: filtros || [],
            ACTION: 'setFinalizarAtendimento',
        };

        app.loading = true;

        var criar_toast = criar_toast || this.criarToast;
        var valida_resize_home = valida_resize_home || this.validaResizeHome;
        var valida_resize_atendimentos = valida_resize_atendimentos || this.validaResizeAtendimentos;
        var buscar_atendimentos = buscar_atendimentos || this.buscarAtendimentos;
        var valida_loading_function = valida_loading_function || this.validaLoading;

        try {
            $.get(`${__SERVER__}/model/atendimentos/atendimentos.php`, data, (data) => {
                data = JSON.parse(data);
                if (data != '' && data != undefined) {
                    if (data[0].tipo == 'sucesso') {
                        criar_toast('2000', 'Sucesso!', 'O atendimento foi finalizado.', 'fas fa-check', 'green', 'id_sucesso');
                    } else {
                        criar_toast('4000', 'Erro!', 'Ocorreu um erro ao finalizar o atendimento. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
                    }
                }
                return buscar_atendimentos(SLICE_ATENDIMENTO, false, [], criar_toast, true, false, valida_resize_home, valida_resize_atendimentos, valida_loading_function, validar_mensagem_lida);
            }).fail((e) => {
                buscar_atendimentos(SLICE_ATENDIMENTO, false, [], criar_toast, true, false, valida_resize_home, valida_resize_atendimentos, valida_loading_function, validar_mensagem_lida);
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> finalizarAtendimento() -> ${e}`;
        }
    }

    /**
     * Franquias
     */
    aumentarNumeroPorcento(index) {
        const interval = setInterval(() => {
            if ($(`.count_${index}`).length) {
                clearInterval(interval);
                $(`.count_${index}`).each(function () {
                    const counter = $(this).text();
                    $(this).prop('Counter', 0).animate({
                        Counter: counter,
                    }, {
                        duration: 2000,
                        easing: 'swing',
                        step(now) {
                            $(this).text(parseFloat(now).toFixed(1));
                        },
                    });
                });
            }
        }, 200);
    }

    moverBarraPorcentagem(index) {
        const interval = setInterval(() => {
            if ($(`.progress_${index}`).length) {
                clearInterval(interval);
                const porcentagem = ($(`.progress_${index}`).data('progress-percent'));
                $(`.bar_${index}`).stop().animate({
                    width: `${porcentagem}%`,
                }, 1600);
            }
        }, 200);
    }

    buscarFranquias(slice, valida_loading) {
        const data = {
            SLICE: slice || this.slice,
            ACTION: 'getFranquias',
        };

        const criar_toast = this.criarToast;
        const mover_porcentagem = this.moverBarraPorcentagem;
        const aumentar_porcentagem = this.aumentarNumeroPorcento;
        const valida_loading_function = this.validaLoading;

        try {
            $.get(`${__SERVER__}/model/consumos/consumos.php`, data, (data) => {
                data = JSON.parse(data);
                if (data != '' && data != undefined) {
                    if (!data[0].tipo) {
                        router.history.current.matched[0].instances.default.franquias = data;
                    }

                    $(document).ready(function () {
                        for (let x = 0; x < router.history.current.matched[0].instances.default.franquias.length; x++) {
                            mover_porcentagem.call(this, x);
                            aumentar_porcentagem.call(this, x);
                        }
                        if (valida_loading) {
                            LOADING_FRANQUIA = false;
                            valida_loading_function();
                        }
                    });

                    return;
                }

                router.history.current.matched[0].instances.default.franquias = [];

                $(document).ready(() => {
                    if (valida_loading) {
                        LOADING_FRANQUIA = false;
                        valida_loading_function();
                    }
                });
            }).fail((e) => {
                router.history.current.matched[0].instances.default.franquias = [];

                $(document).ready(() => {
                    if (valida_loading) {
                        LOADING_FRANQUIA = false;
                        valida_loading_function();
                    }
                });
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> buscarFranquias() -> ${e}`;
        }
    }

    /** RELATORIOS */

    validaAcessoRelatorios() {
        const data = {
            ACTION: 'getPendenciaQuitacaoDebito',
        };

        $(document).ready(() => {
            validaActiveClass('#pg_relatorios');
            router.history.current.matched[0].instances.default.pg_declaracao_debito = app.pg_declaracao_debito;
        });

        const hs_web = new HotsiteWeb();

        try {
            $.get(`${__SERVER__}/model/relatorios/relatorios.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    data = data;
                }

                if (data != '' && data != undefined) {
                    if (data[0].tipo == 'sucesso') {
                        router.history.current.matched[0].instances.default.possui_debito = false;
                        return hs_web.getRamais();
                    }

                    router.history.current.matched[0].instances.default.possui_debito = true;
                    return hs_web.getRamais();
                }
            }).fail((e) => {
                router.history.current.matched[0].instances.default.possui_debito = true;
                return hs_web.getRamais();
            });
        } catch (e) {
            $(document).ready(() => {
                router.history.current.matched[0].instances.default.possui_debito = true;
            });
            return hs_web.getRamais();
        }
    }

    getRamais() {
        const data = {
            ACTION: 'getRamais',
        };

        try {
            $.get(`${__SERVER__}/model/relatorios/relatorios.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    data = data;
                }
                if (data != '' && data != undefined) {
                    if (data.tipo == 'sucesso') {
                        $(document).ready(() => {
                            app.loading = false;
                        });
                        return router.history.current.matched[0].instances.default.ramais = data.mensagem;
                    }
                    $(document).ready(() => {
                        app.loading = false;
                    });
                    return router.history.current.matched[0].instances.default.ramais = [];
                }
            }).fail((e) => {
                $(document).ready(() => {
                    app.loading = false;
                });
                return router.history.current.matched[0].instances.default.ramais = [];
            });
        } catch (e) {
            $(document).ready(() => {
                app.loading = false;
                router.history.current.matched[0].instances.default.ramais = [];
            });
            return `Exception Class HotsiteWeb -> buscarFaturas() -> ${e}`;
        }
    }

    getPeriodosRamal(ramal_selecionado) {
        const data = {
            ACTION: 'getPeriodosRamal',
            RAMAL: ramal_selecionado,
        };

        try {
            $.get(`${__SERVER__}/model/relatorios/relatorios.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    data = data;
                }
                if (data != '' && data != undefined) {
                    if (data.tipo == 'sucesso') {
                        $(document).ready(() => {
                            app.loading = false;
                        });
                        router.history.current.matched[0].instances.default.periodo_selecionado = 'Selecione um período...';
                        return router.history.current.matched[0].instances.default.periodos = data.mensagem;
                    }
                    $(document).ready(() => {
                        app.loading = false;
                    });
                    router.history.current.matched[0].instances.default.periodo_selecionado = 'Selecione um período...';
                    return router.history.current.matched[0].instances.default.periodos = [];
                }
            }).fail((e) => {
                $(document).ready(() => {
                    app.loading = false;
                });
                router.history.current.matched[0].instances.default.periodo_selecionado = 'Selecione um período...';
                return router.history.current.matched[0].instances.default.periodos = [];
            });
        } catch (e) {
            $(document).ready(() => {
                app.loading = false;
                router.history.current.matched[0].instances.default.periodo_selecionado = 'Selecione um período...';
                router.history.current.matched[0].instances.default.periodos = [];
            });
            return `Exception Class HotsiteWeb -> buscarFaturas() -> ${e}`;
        }
    }

    imprimirDetalhamento(id_contrato) {
        this.restoreModalVariavel();
        const criar_toast = this.criarToast;
        const data = {
            ID_CONTRATO: id_contrato || 0,
            APP: app.cordova_app ? 'S' : 'N',
            ACTION: 'getDetalhamentoFinanceiro',
        };

        app.loading_modal = true;

        try {
            if (data.APP == 'S') {
                const fileTransfer = new FileTransfer();
                const fileURL = `${cordova.file.dataDirectory}detalhamento.pdf`;
                const uri = `${__SERVER__}/model/planos/planos.php?ID_CONTRATO=${data.ID_CONTRATO}&APP=${data.APP}&ACTION=${data.ACTION}`;

                fileTransfer.download(uri, fileURL,
                    (entry) => {
                        cordova.plugins.fileOpener2.open(entry.toURL(), 'application/pdf', {
                            error(e) {
                                app.loading = false;
                                alert('Instale um leitor de arquivo .pdf!');
                            },
                            success(e) {
                                app.loading = false;
                            },
                        });
                    },
                    (e) => {
                        app.loading = false;
                        alert('Erro ao baixar arquivo!');
                    }, false, {});
            } else {
                $.get(`${__SERVER__}/model/planos/planos.php`, data, (data) => {
                    try {
                        data = JSON.parse(data);
                    } catch (e) {
                        data = data;
                    }
                    if (data != '' && data != undefined) {
                        if (typeof window.ReactNativeWebView !== 'undefined') {
                            $('#btn_imprimir_fat, #btn_imprimir_fat_, #btn_imprimir_fat_home, #btn_imprimir_fat_home_').attr('disabled', false);
                            document.getElementById('modalImpressaoClose').click();
                            postMessageMobile('base64', JSON.stringify({
                                base: data[0].mensagem.base_pdf,
                                nome: btoa(`${Math.floor(Math.random() * 9999) + 1000}detalhamento.pdf`),
                            })).then(
                                (retorno) => {
                                    $('#btn_imprimir_fat_').attr('disabled', false);
                                    if (JSON.parse(retorno.data).retorno == 'success') {
                                        criar_toast('2000', 'Sucesso!', 'O seu download foi completado com êxito', 'fas fa-check', 'green', 'id_sucesso');
                                    } else {
                                        criar_toast('2000', 'Erro!', `Ocorreu algum erro: ${data[0].mensagem}`, 'fas fa-exclamation-circle', 'red', 'id_erro');
                                    }
                                },
                            );
                        } else {
                            if (data[0].tipo == 'sucesso') {
                                $(document).ready(() => {
                                    app.loading = false;
                                    app.loading_modal = false;
                                });
                                if (navigator.userAgent.match(/Android|BlackBerry|iPhone|iPad|iPod|IEMobile/i)) {
                                    const link = document.createElement('a');
                                    link.href = `data:application/octet-stream;base64,${data[0].mensagem.base_pdf}`;
                                    link.download = 'Detalhamento.pdf';
                                    link.click();
                                    document.getElementById('modalImpressaoClose').click();
                                } else {
                                    return app.modal_url = data[0].mensagem.base_pdf;
                                }
                            } else {
                                criar_toast('2000', 'Erro!', `Ocorreu algum erro:${data[0].mensagem}`, 'fas fa-exclamation-circle', 'red', 'id_erro');
                            }

                            $(document).ready(() => {
                                app.loading = false;
                                app.loading_modal = false;
                            });
                            return app.modal_url = '';
                        }
                    }
                }).fail((e) => {
                    $(document).ready(() => {
                        app.loading = false;
                        app.loading_modal = false;
                    });
                    return app.modal_url = '';
                });
            }
        } catch (e) {
            return `Exception Class HotsiteWeb -> imprimirDetalhamento() -> ${e}`;
        }
    }

    getAnosQuitacao() {
        const data = { ACTION: 'getAnosQuitacao' };
        try {
            app.loading = true;
            $.get(`${__SERVER__}/model/relatorios/relatorios.php`, data, (response) => {
                $(document).ready(() => app.loading = false);
                const result = JSON.parse(response);
                if (result && result.tipo === 'sucesso' && result.mensagem.length) {
                    router.history.current.matched[0].instances.default.ano_selecionado = result.mensagem[0];
                    return router.history.current.matched[0].instances.default.anos = result.mensagem;
                }
                return router.history.current.matched[0].instances.default.anos = [];
            }).fail(() => {
                $(document).ready(() => app.loading = false);
                return router.history.current.matched[0].instances.default.anos = [];
            });
        } catch (e) {
            $(document).ready(() => app.loading = false);
            router.history.current.matched[0].instances.default.anos = [];
            return `Exception Class HotsiteWeb -> getAnosQuitacao() -> ${e}`;
        }
    }

    imprimirExtratoVoip(ramal_selecionado, periodo_selecionado, valida_relatorio) {
        this.restoreModalVariavel();

        if (valida_relatorio === undefined) {
            this.validaTipoRelatorio(ramal_selecionado, periodo_selecionado);
        } else {
            const data = {
                ACTION: 'getExtratoVoip',
                RAMAL: ramal_selecionado,
                PERIODO: periodo_selecionado,
                APP: app.cordova_app ? 'S' : 'N',
            };

            try {
                if (data.APP === 'S') {
                    const fileTransfer = new FileTransfer();
                    const fileURL = `${cordova.file.dataDirectory}relatorio.pdf`;
                    const uri = `${__SERVER__}/model/relatorios/relatorios.php?RAMAL=${data.RAMAL}&PERIODO=${data.PERIODO}&APP=${data.APP}&ACTION=${data.ACTION}`;

                    fileTransfer.download(uri, fileURL,
                        (entry) => {
                            cordova.plugins.fileOpener2.open(entry.toURL(), 'application/pdf', {
                                error(e) {
                                    app.loading = false;
                                    alert('Instale um leitor de arquivo .pdf!');
                                },
                                success(e) {
                                    app.loading = false;
                                },
                            });
                        },
                        (e) => {
                            app.loading = false;
                            alert('Erro ao baixar arquivo!');
                        }, false, {});
                } else {
                    $.get(`${__SERVER__}/model/relatorios/relatorios.php`, data, (data) => {
                        try {
                            data = JSON.parse(data);
                        } catch (e) {
                            data = data;
                        }
                        if (data != '' && data != undefined) {
                            if (typeof window.ReactNativeWebView !== 'undefined') {
                                $('#btn_imprimir_fat, #btn_imprimir_fat_, #btn_imprimir_fat_home, #btn_imprimir_fat_home_').attr('disabled', false);
                                document.getElementById('modalImpressaoClose').click();
                                postMessageMobile('base64', JSON.stringify({
                                    base: data[0].mensagem.base_pdf,
                                    nome: btoa((Math.floor(Math.random() * 9999) + 1000)) + data[1],
                                })).then(
                                    (retorno) => {
                                        $('#btn_imprimir_fat_').attr('disabled', false);
                                        if (JSON.parse(retorno.data).retorno == 'success') {
                                            criar_toast('2000', 'Sucesso!', 'O seu download foi completado com êxito', 'fas fa-check', 'green', 'id_sucesso');
                                        } else {
                                            criar_toast('2000', 'Erro!', 'Ocorreu algum erro ao fazer o download', 'fas fa-exclamation-circle', 'red', 'id_erro');
                                        }
                                    },
                                );
                            } else {
                                if (data[0].tipo == 'sucesso') {
                                    $(document).ready(() => {
                                        app.loading = false;
                                        app.loading_modal = false;
                                    });
                                    return app.modal_url = data[0].mensagem.base_pdf;
                                }

                                $(document).ready(() => {
                                    app.loading = false;
                                    app.loading_modal = false;
                                });
                                return app.modal_url = '';
                            }
                        }
                    }).fail((e) => {
                        $(document).ready(() => {
                            app.loading = false;
                            app.loading_modal = false;
                        });
                        return app.modal_url = '';
                    });
                }
            } catch (e) {
                $(document).ready(() => {
                    app.loading = false;
                    app.loading_modal = false;
                    app.modal_url = '';
                });
                return `Exception Class HotsiteWeb -> buscarFaturas() -> ${e}`;
            }
        }
    }

    validaTipoRelatorio(ramal_selecionado, periodo_selecionado) {
        const tipo_relatorio = $('#tipo_relatorio_extrato_ligacao option:selected').val();

        const hs_web = new HotsiteWeb();

        if (tipo_relatorio === 'extrato_ligacoes') {
            hs_web.imprimirExtratoVoip(ramal_selecionado, periodo_selecionado, true);
        } else if (tipo_relatorio === 'extrato_detalhado') {
            hs_web.imprimirExtratoDetalhadoVoip(ramal_selecionado, periodo_selecionado);
        }
    }

    /** Imprimir Extrato Detalhado VOIP */
    imprimirExtratoDetalhadoVoip(ramal_selecionado, periodo_selecionado) {
        this.restoreModalVariavel();

        const data = {
            ACTION: 'getExtratoDetalhadoVoip',
            RAMAL: ramal_selecionado,
            PERIODO: periodo_selecionado,
            APP: app.cordova_app ? 'S' : 'N',
        };

        try {
            if (data.APP === 'S') {
                const fileTransfer = new FileTransfer();
                const fileURL = `${cordova.file.dataDirectory}relatorio.pdf`;
                const uri = `${__SERVER__}/model/relatorios/relatorios.php?RAMAL=${data.RAMAL}&PERIODO=${data.PERIODO}&APP=${data.APP}&ACTION=${data.ACTION}`;

                fileTransfer.download(uri, fileURL,
                    (entry) => {
                        cordova.plugins.fileOpener2.open(entry.toURL(), 'application/pdf', {
                            error(e) {
                                app.loading = false;
                                alert('Instale um leitor de arquivo .pdf!');
                            },
                            success(e) {
                                app.loading = false;
                            },
                        });
                    },
                    (e) => {
                        app.loading = false;
                        alert('Erro ao baixar arquivo!');
                    }, false, {});
            } else {
                $.get(`${__SERVER__}/model/relatorios/relatorios.php`, data, (data) => {
                    try {
                        data = JSON.parse(data);
                    } catch (e) {
                        data = data;
                    }
                    if (data != '' && data != undefined) {
                        if (typeof window.ReactNativeWebView !== 'undefined') {
                            $('#btn_imprimir_fat, #btn_imprimir_fat_, #btn_imprimir_fat_home, #btn_imprimir_fat_home_').attr('disabled', false);
                            document.getElementById('modalImpressaoClose').click();
                            postMessageMobile('base64', JSON.stringify({
                                base: data[0].mensagem.base_pdf,
                                nome: btoa((Math.floor(Math.random() * 9999) + 1000)) + data[1],
                            })).then(
                                (retorno) => {
                                    $('#btn_imprimir_fat_').attr('disabled', false);
                                    if (JSON.parse(retorno.data).retorno == 'success') {
                                        criar_toast('2000', 'Sucesso!', 'O seu download foi completado com êxito', 'fas fa-check', 'green', 'id_sucesso');
                                    } else {
                                        criar_toast('2000', 'Erro!', 'Ocorreu algum erro ao fazer o download', 'fas fa-exclamation-circle', 'red', 'id_erro');
                                    }
                                },
                            );
                        } else {
                            if (data[0].tipo == 'sucesso') {
                                $(document).ready(() => {
                                    app.loading = false;
                                    app.loading_modal = false;
                                });
                                return app.modal_url = data[0].mensagem.base_pdf;
                            }

                            $(document).ready(() => {
                                app.loading = false;
                                app.loading_modal = false;
                            });
                            return app.modal_url = '';
                        }
                    }
                }).fail((e) => {
                    $(document).ready(() => {
                        app.loading = false;
                        app.loading_modal = false;
                    });
                    return app.modal_url = '';
                });
            }
        } catch (e) {
            $(document).ready(() => {
                app.loading = false;
                app.loading_modal = false;
                app.modal_url = '';
            });
            return `Exception Class HotsiteWeb -> buscarFaturas() -> ${e}`;
        }
    }

    /**
     * SESSAO USER
     */
    validaFNProject() {
        const data = {
            ACTION: 'getValidaFNProject',
        };

        const hs_web = new HotsiteWeb();

        $.ajax({
            url: `${__SERVER__}/model/login/login.php`,
            type: 'GET',
            data,
            dataType: 'JSON',
            async: false,
        }).done((data) => {
            if (data != '' && data != undefined && data.tipo === 'sucesso') {
                if (!data.mensagem) {
                    return hs_web.validaIPSessao();
                }
                CAD_LOGIN = true;
                return;
            }
            CAD_LOGIN = true;
        });
    }

    getRecuperaSenhaTipo() {
        const data = {
            ACTION: 'getRecuperaSenhaTipo',
        };
        return new Promise((resolve) => {
            try {
                $.get(`${__SERVER__}/model/login/login.php`, data, (data) => {
                    data = JSON.parse(data);
                    router.history.current.matched[0].instances.default.recupera_senha_tipo = data.recupera_senha;
                    resolve(data.recupera_senha);
                }).fail((e) => {
                    router.replace('/central_assinante_web/login');
                    criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
                });
            } catch (e) {
                resolve(`Exception Class HotsiteWeb -> getRecuperaSenhaTipo() -> ${e}`);
            }
        });
    }

    validaCamposRecuperaSenha(login, cnpj_cpf = '', telefone = '') {
        const data = {
            ACTION: 'validaCamposRecuperaSenha',
            USER: login,
            CNPJ_CPF: cnpj_cpf,
            TELEFONE: telefone,
        };
        return new Promise((resolve) => {
            try {
                $.get(`${__SERVER__}/model/login/login.php`, data, (data) => {
                    data = JSON.parse(data);
                    resolve(data);
                }).fail((e) => {
                    router.replace('/central_assinante_web/login');
                    criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
                });
            } catch (e) {
                resolve(`Exception Class HotsiteWeb -> getLoginHotsite() -> ${e}`);
            }
        });
    }

    buscarTipoLogin(criar_toast) {
        const data = {
            ID_CLIENTE: 0,
            ACTION: 'getTipoLogin',
        };

        var criar_toast = criar_toast || this.criarToast;

        try {
            $.get(`${__SERVER__}/model/login/login.php`, data, (data) => {
                data = JSON.parse(data);

                if (data != '' && data != undefined) {
                    if (data[0].tipo == 'sucesso') {
                        app.tipo_login = data[0].mensagem.tipo_login;
                        app.permitir_cadastros_usuario = data[0].mensagem.permitir_cadastros_usuario;
                        const logo = data[0].mensagem.hotsite_logo_base;
                        $(document).ready(() => {
                            router.history.current.matched[0].instances.default.label_login = data[0].mensagem.login_campo_texto;
                            router.history.current.matched[0].instances.default.tipo_login = data[0].mensagem.tipo_login;
                            router.history.current.matched[0].instances.default.permitir_cadastros_usuario = data[0].mensagem.permitir_cadastros_usuario;
                            $('.tipo_login_senha').removeClass('display-none');
                            $('.login-esqueceu-senha').removeClass('display-none');

                            if (logo) {
                                router.history.current.matched[0].instances.default.logo_base_login = `data:application/image;base64,${data[0].mensagem.hotsite_logo_base}`;
                            } else {
                                router.history.current.matched[0].instances.default.logo_base_login = `${__SERVER__}/assets/img/logo_padrao.png`;
                            }

                            const { tipo_login } = router.history.current.matched[0].instances.default;
                            if (tipo_login == 'C') {
                                router.history.current.matched[0].instances.default.mask = true;
                            } else {
                                router.history.current.matched[0].instances.default.mask = false;
                            }
                            router.replace('/central_assinante_web/login');
                        });
                    }
                }
            }).fail((e) => {
                router.replace('/central_assinante_web/login');
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> buscarTipoLogin() -> ${e}`;
        }
    }

    proximaEtapa(etapa) {
        router.history.current.matched[0].instances.default.wizard = ((etapa == 'A' || etapa == 'C') ? etapa : 'A');

        setTimeout(() => router.history.current.matched[0].instances.default.mostrar_tela = ((etapa == 'A' || etapa == 'C') ? etapa : 'A'), 250);
    }

    pegarDadosSessao() {
        const data = {
            ACTION: 'getValidaLoginHash',
        };

        const hs_web = new HotsiteWeb();

        $.ajax({
            url: `${__SERVER__}/model/login/login.php`,
            type: 'GET',
            data,
            dataType: 'JSON',
            async: false,
        }).done((data) => {
            if (data != '' && data != undefined && data[0].tipo === 'sucesso') {
                if (data[0].mensagem.dados_cliente.acesso_automatico_central === 'S') {
                    if (data[0].mensagem.dados_cliente.primeiro_acesso_central === 'S') {
                        hs_web.buscarLogo();

                        $(document).ready(() => {
                            hs_web.proximaEtapa('A');
                            router.history.current.matched[0].instances.default.cliente_nome = data[0].mensagem.dados_cliente.razao;
                        });

                        CAD_LOGIN = false;
                        return;
                    }

                    if ((!data[0].mensagem.dados_cliente.hotsite_email || !data[0].mensagem.dados_cliente.senha) && data[0].mensagem.parametros_hotsite.tipo_login === 'E') {
                        hs_web.buscarLogo();
                        $(document).ready(() => {
                            hs_web.proximaEtapa('C');
                        });
                        CAD_LOGIN = false;
                        return;
                    }

                    hs_web.setPrimeiroAcessoOK();
                    hs_web.criarSessao(data);
                    CAD_LOGIN = true;
                    $(document).ready(() => {
                        router.history.current.matched[0].instances.default.loading_one = false;
                    });
                    return;
                } if (data[0].mensagem.dados_cliente.acesso_automatico_central === 'P' && data[0].mensagem.parametros_hotsite.acesso_automatico_central === 'S') {
                    if (data[0].mensagem.dados_cliente.primeiro_acesso_central === 'S') {
                        hs_web.buscarLogo();

                        $(document).ready(() => {
                            hs_web.proximaEtapa('A');
                            router.history.current.matched[0].instances.default.cliente_nome = data[0].mensagem.dados_cliente.razao;
                        });

                        CAD_LOGIN = false;
                        return;
                    }

                    if ((!data[0].mensagem.dados_cliente.hotsite_email || !data[0].mensagem.dados_cliente.senha) && data[0].mensagem.parametros_hotsite.tipo_login === 'E') {
                        hs_web.buscarLogo();
                        $(document).ready(() => {
                            hs_web.proximaEtapa('C');
                        });
                        CAD_LOGIN = false;
                        return;
                    }

                    hs_web.setPrimeiroAcessoOK();
                    hs_web.criarSessao(data);
                    CAD_LOGIN = true;
                    $(document).ready(() => {
                        router.history.current.matched[0].instances.default.loading_one = false;
                    });
                    return;
                }
                CAD_LOGIN = true;
                return true;
            }
            $(document).ready(() => {
                router.history.current.matched[0].instances.default.loading_one = false;
            });
            CAD_LOGIN = true;
        });
    }

    validarDadosUsuarioPrimeiroAcesso() {
        const data = {
            ACTION: 'getValidaLoginHash',
        };

        const hs_web = new HotsiteWeb();

        $.ajax({
            url: `${__SERVER__}/model/login/login.php`,
            type: 'GET',
            data,
            dataType: 'JSON',
            async: false,
        }).done((data) => {
            if (data != '' && data != undefined && data[0].tipo === 'sucesso') {
                if ((data[0].mensagem.dados_cliente.acesso_automatico_central === 'S') || (data[0].mensagem.dados_cliente.acesso_automatico_central === 'P' && data[0].mensagem.parametros_hotsite.acesso_automatico_central === 'S')) {
                    if ((!data[0].mensagem.dados_cliente.hotsite_email || !data[0].mensagem.dados_cliente.senha) && data[0].mensagem.parametros_hotsite.tipo_login === 'E') {
                        CAD_LOGIN = false;
                        return hs_web.solicitarCadastroUser();
                    }

                    hs_web.setPrimeiroAcessoOK();
                    hs_web.criarSessao(data);
                    CAD_LOGIN = true;
                }
            } else {
                $(document).ready(() => {
                    router.history.current.matched[0].instances.default.loading_one = false;
                });
                CAD_LOGIN = true;
            }
        });
    }

    solicitarCadastroUser() {
        $(document).ready(() => {
            const hs_web = new HotsiteWeb();

            router.history.current.matched[0].instances.default.loading_one = false;
            hs_web.buscarLogo();
            hs_web.proximaEtapa('C');
        });
    }

    buscarLogo() {
        const data = {
            ID_CLIENTE: 0,
            ACTION: 'getTipoLogin',
        };

        try {
            $.get(`${__SERVER__}/model/login/login.php`, data, (data) => {
                data = JSON.parse(data);

                if (data != '' && data != undefined) {
                    if (data[0].tipo == 'sucesso') {
                        const logo = data[0].mensagem.hotsite_logo_base;
                        $(document).ready(() => {
                            if (logo) {
                                router.history.current.matched[0].instances.default.logo_base_login = `data:application/image;base64,${data[0].mensagem.hotsite_logo_base}`;
                            } else {
                                router.history.current.matched[0].instances.default.logo_base_login = `${__SERVER__}/assets/img/logo_padrao.png`;
                            }
                            app.loading = false;
                        });
                    }
                }
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> buscarLogo() -> ${e}`;
        }
    }

    setPrimeiroAcessoOK() {
        const data = {
            ACTION: 'setPrimeiroAcessoOK',
        };

        $.ajax({
            url: `${__SERVER__}/model/login/login.php`,
            type: 'GET',
            data,
            dataType: 'JSON',
        });
    }

    criarSessao(data) {
        sessionStorage.setItem('dados', JSON.stringify(data[0].mensagem.dados_cliente));
        sessionStorage.setItem('parametros', JSON.stringify(data[0].mensagem.parametros_hotsite));
        sessionStorage.setItem('sessao', JSON.stringify(data[0].mensagem.sessao));
        localStorage.setItem('manter_conectado', $.cookie('manter_conectado') || 'N');
        $.cookie('sessao', data[0].mensagem.sessao);

        __DADOS_CLIENTE__.push(data[0].mensagem.dados_cliente);
        __PARAMETROS__.push(data[0].mensagem.parametros_hotsite);

        let parametros = null;
        let dados_cliente = null;

        parametros = JSON.parse(sessionStorage.getItem('parametros'));
        dados_cliente = JSON.parse(sessionStorage.getItem('dados'));

        const fantasia = dados_cliente.fantasia.split(' ')[0];
        const razao = dados_cliente.razao.split(' ')[0];
        const cliente_nome = fantasia || razao;

        app.cliente_nome = cliente_nome;
        app.pg_fatura = parametros.habilitar_financeiro;
        app.pg_plano = parametros.habilitar_contratos;
        app.pg_nota = parametros.mostra_menu_nota_app;
        app.pg_consumo = parametros.habilitar_extrato_internet;
        app.pg_atendimento = parametros.habilitar_suporte;
        app.pg_declaracao_debito = parametros.habilitar_declaracao_debitos;
        app.pg_alterar_senha = parametros.habilitar_altera_senha;
        app.pg_exibir_detalhamento_financeiro = parametros.exibir_detalhamento_financeiro;
        app.pg_connections = parametros.habilitar_menu_conexao;

        app.url_app_android = parametros.url_app_android;

        app.mostra_builder = parametros.FN;
        app.is_fn = parametros.FN;
        app.habilitar_contato = parametros.habilitar_contato;
        app.whatsapp_link = parametros.whatsapp_link;
        app.webchat_link = parametros.webchat_link;
        app.telefone_contato = parametros.telefone_contato;

        if (parametros.hotsite_logo_base) {
            app.logo_base = `data:application/image;base64,${parametros.hotsite_logo_base}`;
        } else {
            app.logo_base = `${__SERVER__}/assets/img/logo.png`;
        }

        FATURA_TOAST = true;
        PLANO_TOAST = true;
        ATENDIMENTO_TOAST = true;

        app.habilitar_form_primeiro_acesso = false;
        if (dados_cliente.primeiro_acesso_central === 'S' && parametros.alterar_senha_primeiro_acesso === 'S' && dados_cliente.alterar_senha_primeiro_acesso !== 'N' && app.tipo_login === 'E') {
            app.habilitar_form_primeiro_acesso = true;
        }

        $(document).ready(() => {
            router.history.current.matched[0].instances.default.loading = false;
            if (app.habilitar_form_primeiro_acesso) {
                new HotsiteWeb().removeCache();
                router.history.current.matched[0].instances.default.habilitar_form_primeiro_acesso = true;
                // new HotsiteWeb().getRecuperaSenhaTipo().then((tipo)=>{
                //     router.history.current.matched[0].instances.default.recupera_senha_tipo=tipo
                router.replace('central_assinante_web/login');
                // });
            } else {
                router.replace('central_assinante_web');
            }
        });
    }

    getTokenAPP() {
        return new Promise((resolve) => {
            let token = '';
            if (typeof window.ReactNativeWebView !== 'undefined') {
                postMessageMobile('firebase_token').then((event) => {
                    if (JSON.parse(event.data).token) {
                        token = JSON.parse(event.data).token;
                    } else {
                        token = JSON.parse(event.data).retorno;
                    }
                    resolve(token);
                });
            } else if (app.cordova_app) {
                window.FirebasePlugin.grantPermission();
                window.FirebasePlugin.getToken((token) => {
                    resolve(token);
                });
            } else {
                resolve();
            }
        });
    }

    validaIPSessao() {
        const hs_web = new HotsiteWeb();
        hs_web.getTokenAPP().then((token) => {
            const isReact = typeof window.ReactNativeWebView !== 'undefined';
            const data = {
                ACTION: 'getValidaIPSessao',
                APP: app.cordova_app || isReact ? 'S' : 'N',
                TOKEN: token,
            };
            $.ajax({
                url: `${__SERVER__}/model/login/login.php`,
                type: 'GET',
                data,
                dataType: 'JSON',
                async: false,
            }).done((data) => {
                const hs_web = new HotsiteWeb();
                if (data != '' && data != undefined && data.tipo === 'sucesso') {
                    $.cookie('sessao', data.mensagem);
                    $.cookie('manter_conectado', 'N');

                    return hs_web.pegarDadosSessao();
                }
                $(document).ready(() => {
                    router.history.current.matched[0].instances.default.loading = false;
                });
                CAD_LOGIN = true;
            });
        });
    }

    validarCriarUsuarioPrimeiroAcesso(email_user, pass_user, pass_two_user) {
        const data = {
            EMAIL: email_user || '',
            SENHA: pass_user || '',
            REPETIR_SENHA: pass_two_user || '',
            ACTION: 'setCadastrarLogin',
        };

        const hs_web = new HotsiteWeb();

        $.ajax({
            url: `${__SERVER__}/model/login/login.php`,
            type: 'GET',
            data,
            dataType: 'JSON',
            async: false,
        }).done((data) => {
            if (data != '' && data != undefined && data[0].tipo === 'sucesso') {
                hs_web.setPrimeiroAcessoOK();
                hs_web.criarSessao(data);
                CAD_LOGIN = true;
            } else {
                hs_web.criarToast('5000', 'Erro!', data[0].mensagem, 'fas fa-exclamation-circle', 'red', 'id_erro');
                $(document).ready(() => {
                    router.history.current.matched[0].instances.default.loading_two = false;
                });
                CAD_LOGIN = true;
            }
        });
    }

    iniciarSessao(user, password) {
        const isReact = typeof window.ReactNativeWebView !== 'undefined';
        const data = {
            ID_CLIENTE: 0,
            USER: user || '',
            PASSWORD: password ? md5(password) : '',
            APP: app.cordova_app || isReact ? 'S' : 'N',
            TOKEN: '',
            ACTION: 'getValidaLogin',
            MANTER_CONNECTADO: router.history.current.matched[0].instances.default.manter_conectado,
        };

        const criar_toast = this.criarToast;
        const request = this.requestValidaLoginIniciarSessao;

        try {
            if (typeof window.ReactNativeWebView !== 'undefined') {
                return postMessageMobile('firebase_token').then((event) => {
                    if (JSON.parse(event.data).token) {
                        data.TOKEN = JSON.parse(event.data).token;
                    } else {
                        data.TOKEN = JSON.parse(event.data).retorno;
                    }
                    request(data, criar_toast);
                });
            } if (app.cordova_app) {
                window.FirebasePlugin.grantPermission();
                window.FirebasePlugin.getToken((token) => {
                    data.TOKEN = token;
                    request(data, criar_toast);
                });

                return;
            }

            request(data, criar_toast);
        } catch (e) {
            return `Exception Class HotsiteWeb -> iniciarSessao() -> ${e}`;
        }
    }

    requestValidaLoginIniciarSessao(dados, criar_toast) {
        const manter_conectado = dados.MANTER_CONNECTADO;
        setTimeout(() => {
            if (router.history.current.matched[0].instances.default.loading) {
                router.replace('/central_assinante_web/');
            }
        }, 15000);

        $.post(`${__SERVER__}/model/login/login.php`, dados, (data) => {
            data = JSON.parse(data);

            if (data != '' && data != undefined) {
                if (data[0].tipo == 'sucesso') {
                    $(document).ready(() => {
                        app.login = false;
                    });

                    if (router.history.current.matched[0].instances.default.manter_conectado === true) {
                        localStorage.setItem('dados', JSON.stringify(data[0].mensagem.dados_cliente));
                        localStorage.setItem('parametros', JSON.stringify(data[0].mensagem.parametros_hotsite));
                        localStorage.setItem('sessao', JSON.stringify(data[0].mensagem.sessao));
                        localStorage.setItem('manter_conectado', $.cookie('manter_conectado'));
                        $.cookie('sessao', data[0].mensagem.sessao);
                    } else {
                        sessionStorage.setItem('dados', JSON.stringify(data[0].mensagem.dados_cliente));
                        sessionStorage.setItem('parametros', JSON.stringify(data[0].mensagem.parametros_hotsite));
                        sessionStorage.setItem('sessao', JSON.stringify(data[0].mensagem.sessao));
                        $.cookie('sessao', data[0].mensagem.sessao);
                    }

                    __DADOS_CLIENTE__.push(data[0].mensagem.dados_cliente);
                    __PARAMETROS__.push(data[0].mensagem.parametros_hotsite);

                    let parametros = null;
                    let dados_cliente = null;

                    if (router.history.current.matched[0].instances.default.manter_conectado === true) {
                        parametros = JSON.parse(localStorage.getItem('parametros'));
                        dados_cliente = JSON.parse(localStorage.getItem('dados'));
                    } else {
                        parametros = JSON.parse(sessionStorage.getItem('parametros'));
                        dados_cliente = JSON.parse(sessionStorage.getItem('dados'));
                    }

                    const fantasia = dados_cliente.fantasia.split(' ')[0];
                    const razao = dados_cliente.razao.split(' ')[0];
                    const identifica_nome = parametros.identificacao_central_assinante;
                    const cliente_nome = (identifica_nome === 'F' ? (fantasia || razao) : razao);

                    app.pg_fatura = parametros.habilitar_financeiro;
                    app.pg_plano = parametros.habilitar_contratos;
                    app.pg_nota = parametros.mostra_menu_nota_app;
                    app.pg_consumo = parametros.habilitar_extrato_internet;
                    app.pg_atendimento = parametros.habilitar_suporte;
                    app.pg_declaracao_debito = parametros.habilitar_declaracao_debitos;
                    app.pg_alterar_senha = parametros.habilitar_altera_senha;
                    app.pg_exibir_detalhamento_financeiro = parametros.exibir_detalhamento_financeiro;
                    app.pg_connections = parametros.habilitar_menu_conexao;
                    app.habilitar_recorrencia = parametros.habilitar_recorrencia !== 'N';

                    app.url_app_android = parametros.url_app_android;

                    app.mostra_builder = parametros.FN;
                    app.is_fn = parametros.FN;
                    app.habilitar_contato = parametros.habilitar_contato;
                    app.whatsapp_link = parametros.whatsapp_link;
                    app.webchat_link = parametros.webchat_link;
                    app.telefone_contato = parametros.telefone_contato;

                    if (dados_cliente.primeiro_acesso_central === 'S' && parametros.alterar_senha_primeiro_acesso === 'S' && dados_cliente.alterar_senha_primeiro_acesso !== 'N' && app.tipo_login === 'E') {
                        app.habilitar_form_primeiro_acesso = true;
                    }

                    if (parametros.FRANQUIA) {
                        if (parametros.HOSTFRANQUIA) {
                            const form = document.createElement('form');
                            form.method = 'POST';
                            form.action = `${parametros.HOSTFRANQUIA}central_assinante_web/model/login/login.php`;

                            const input = document.createElement('input');
                            input.name = 'USER';
                            input.value = dados.USER;

                            form.appendChild(input);

                            const token = document.createElement('input');
                            token.name = 'TOKEN';
                            token.value = dados.TOKEN;

                            form.appendChild(token);

                            const senha = document.createElement('input');
                            senha.name = 'PASSWORD';
                            senha.value = dados.PASSWORD;

                            form.appendChild(senha);

                            const action = document.createElement('input');
                            action.name = 'ACTION';
                            action.value = 'getValidaLogin2';

                            form.appendChild(action);

                            const manter_conectado = document.createElement('input');
                            manter_conectado.name = 'MANTER_CONECTADO';
                            manter_conectado.value = router.history.current.matched[0].instances.default.manter_conectado ? 'S' : 'N';

                            form.appendChild(manter_conectado);

                            document.body.appendChild(form);
                            form.submit();
                        }
                    } else {
                        if (parametros.hotsite_logo_base) {
                            app.logo_base = `data:application/image;base64,${parametros.hotsite_logo_base}`;
                        } else {
                            app.logo_base = `${__SERVER__}/assets/img/logo.png`;
                        }

                        FATURA_TOAST = true;
                        PLANO_TOAST = true;
                        ATENDIMENTO_TOAST = true;

                        $(document).ready(() => {
                            router.history.current.matched[0].instances.default.loading = false;
                            if (app.habilitar_form_primeiro_acesso) {
                                app.login = false;
                                new HotsiteWeb().removeCache();
                                router.history.current.matched[0].instances.default.habilitar_form_primeiro_acesso = true;
                                // new HotsiteWeb().getRecuperaSenhaTipo().then((tipo)=>{
                                //     router.history.current.matched[0].instances.default.recupera_senha_tipo=tipo
                                router.replace('central_assinante_web/login');
                                // });
                            } else {
                                router.replace('central_assinante_web');
                            }
                        });
                    }
                } else {
                    router.history.current.matched[0].instances.default.loading = false;
                    criar_toast('5000', 'Erro!', data[0].mensagem, 'fas fa-exclamation-circle', 'red', 'id_erro', true, '', 'topCenter');
                }
            } else {
                router.history.current.matched[0].instances.default.loading = false;
                criar_toast('5000', 'Erro!', data[0].mensagem, 'fas fa-exclamation-circle', 'red', 'id_erro', true, '', 'topCenter');
            }
        }).fail((e) => {
            router.history.current.matched[0].instances.default.loading = false;
            criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
        });
    }

    validaSessao() {
        app.login = true;
        let sessao_existe = false;
        const hs_web = new HotsiteWeb();

        if (localStorage.getItem('manter_conectado')) {
            $.cookie('manter_conectado', localStorage.getItem('manter_conectado'));
        }

        let dados = null;
        if (JSON.parse(localStorage.getItem('dados')) || JSON.parse(sessionStorage.getItem('dados'))) {
            if ($.cookie('manter_conectado') == 'S') {
                dados = JSON.parse(localStorage.getItem('dados'));
            } else {
                dados = JSON.parse(sessionStorage.getItem('dados'));
            }

            if (!dados) {
                hs_web.removeCache();
                return;
            }
        }

        let parametros = null;
        let cacheParams = {
            local: JSON.parse(localStorage.getItem('parametros')),
            session: JSON.parse(sessionStorage.getItem('parametros')),
        }

        if (cacheParams.local || cacheParams.session) {
            parametros = ($.cookie('manter_conectado') == 'S' && cacheParams.local) ? cacheParams.local : cacheParams.session
            if (parametros.hotsite_logo_base) {
                app.logo_base = `data:application/image;base64,${parametros.hotsite_logo_base}`;
            } else {
                app.logo_base = `${__SERVER__}/assets/img/logo.png`;
            }
        }

        if (dados && parametros) {
            const fantasia = dados.fantasia.split(' ')[0];
            const razao = dados.razao.split(' ')[0];
            const identifica_nome = parametros.identificacao_central_assinante;
            const cliente_nome = (identifica_nome === 'F' ? (fantasia || razao) : razao);
            app.cliente_nome = cliente_nome;
        }

        if (localStorage.getItem('sessao') || sessionStorage.getItem('sessao')) {
            var sessao = null;

            if ($.cookie('manter_conectado') == 'S') {
                sessao = JSON.parse(localStorage.getItem('sessao'));
            } else {
                sessao = JSON.parse(sessionStorage.getItem('sessao'));
            }

            if (sessao && !$.cookie('sessao')) {
                $.cookie('sessao', sessao);
            }

            app.sessao = sessao;
        }

        if (dados && parametros && sessao && $.cookie('manter_conectado')) {
            const dados_consulta = 'ACTION=getValidaSessao';
            $.ajax({
                url: `${__SERVER__}/model/login/login.php`,
                type: 'POST',
                data: dados_consulta,
                dataType: 'JSON',
                async: false,
            }).done((data) => {
                if (data[0].tipo == 'sucesso') {
                    if (data[0].mensagem.FRANQUIA) {
                        if (data[0].mensagem.HOSTFRANQUIA) {
                            window.location.href = `${data[0].mensagem.HOSTFRANQUIA}central_assinante_web/`;
                        } else {
                            const hs_web = new HotsiteWeb();
                            hs_web.encerrarSessaoHotsite();
                            sessao_existe = false;
                        }
                    }
                    sessao_existe = true;
                } else {
                    sessao_existe = false;
                }
            });
        }

        if (sessao_existe) {
            app.login = false;
            app.loading = false;
            return true;
        }

        if ($.cookie('manter_conectado') == 'S') {
            sessionStorage.removeItem('dados');
            sessionStorage.removeItem('parametros');
            localStorage.removeItem('parametros');
            sessionStorage.removeItem('sessao');
            sessionStorage.removeItem('manter_conectado');
            $.removeCookie('manter_conectado');
            $.removeCookie('sessao');
        }

        app.login = true;
        app.loading = false;
        return false;
    }

    encerrarSessaoHotsite() {
        const data = {
            ID_CLIENTE: 0,
            ACTION: 'setEncerrarSessao',
        };

        const criar_toast = this.criarToast;
        const buscar_tipo_login = this.buscarTipoLogin;

        try {
            $.get(`${__SERVER__}/model/login/login.php`, data, (data) => {
                data = JSON.parse(data);

                if (data != '' && data != undefined) {
                    if (data[0].tipo == 'sucesso') {
                        if ($.cookie('manter_conectado') == 'S') {
                            localStorage.removeItem('dados');
                            localStorage.removeItem('parametros');
                            sessionStorage.removeItem('parametros');
                            localStorage.removeItem('sessao');
                            localStorage.removeItem('manter_conectado');
                        } else {
                            sessionStorage.removeItem('dados');
                            localStorage.removeItem('parametros');
                            sessionStorage.removeItem('parametros');
                            sessionStorage.removeItem('sessao');
                            sessionStorage.removeItem('manter_conectado');
                        }

                        $.removeCookie('manter_conectado');
                        $.removeCookie('sessao');

                        __DADOS_CLIENTE__.splice(0, __DADOS_CLIENTE__.length);
                        __PARAMETROS__.splice(0, __PARAMETROS__.length);
                        app.login = true;
                        buscar_tipo_login(criar_toast);
                    } else {
                        criar_toast('2000', 'Erro!', 'Erro ao encerrar a sessao', 'fas fa-exclamation-circle', 'red', 'id_erro');
                        router.replace('central_assinante_web/login');
                    }
                }
            }).fail((e) => {
                criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> encerrarSessaoHotsite() -> ${e}`;
        }
    }

    validaExisteDados() {
        if ((localStorage.getItem('dados') && localStorage.getItem('parametros')) || (sessionStorage.getItem('dados') && sessionStorage.getItem('parametros'))) {
            if (!__DADOS_CLIENTE__.length) {
                if ($.cookie('manter_conectado') == 'S') {
                    __DADOS_CLIENTE__.push(JSON.parse(localStorage.getItem('dados')));
                } else {
                    __DADOS_CLIENTE__.push(JSON.parse(sessionStorage.getItem('dados')));
                }
            }

            if (!__PARAMETROS__.length) {
                if ($.cookie('manter_conectado') == 'S') {
                    __PARAMETROS__.push(JSON.parse(localStorage.getItem('parametros')));
                } else {
                    __PARAMETROS__.push(JSON.parse(sessionStorage.getItem('parametros')));
                }
            }

            if (localStorage.getItem('manter_conectado')) {
                $.cookie('manter_conectado', localStorage.getItem('manter_conectado'));
            }

            let parametros = null;
            let cacheParams = {
                local: JSON.parse(localStorage.getItem('parametros')),
                session: JSON.parse(sessionStorage.getItem('parametros')),
            }
            parametros = ($.cookie('manter_conectado') == 'S' && cacheParams.local) ? cacheParams.local : cacheParams.session;

            app.pg_fatura = parametros.habilitar_financeiro;
            app.pg_plano = parametros.habilitar_contratos;
            app.pg_nota = parametros.mostra_menu_nota_app;
            app.pg_consumo = parametros.habilitar_extrato_internet;
            app.pg_atendimento = parametros.habilitar_suporte;
            app.pg_declaracao_debito = parametros.habilitar_declaracao_debitos;
            app.pg_alterar_senha = parametros.habilitar_altera_senha;
            app.pg_exibir_detalhamento_financeiro = parametros.exibir_detalhamento_financeiro;
            app.pg_connections = parametros.habilitar_menu_conexao;

            app.sms_fatura = parametros.envia_sms_app;
            app.email_fatura = parametros.envia_email_app;
            app.imprime_venda_fatura = parametros.imprime_venda_fatura_app;

            app.cad_cli_fantasia = parametros.cad_cli_fantasia;
            app.cad_cli_cpf = parametros.cad_cli_cpf;
            app.cad_cli_rg = parametros.cad_cli_rg;
            app.cad_cli_data_nascimento = parametros.cad_cli_data_nascimento;
            app.cad_cli_telefone = parametros.cad_cli_telefone;
            app.cad_cli_celular = parametros.cad_cli_celular;
            app.cad_cli_telefone_comercial = parametros.cad_cli_telefone_comercial;
            app.cad_cli_ramal = parametros.cad_cli_ramal;
            app.cad_cli_cep = parametros.cad_cli_cep;
            app.cad_cli_endereco = parametros.cad_cli_endereco;
            app.cad_cli_numero = parametros.cad_cli_numero;
            app.cad_cli_bairro = parametros.cad_cli_bairro;
            app.cad_cli_referencia = parametros.cad_cli_referencia;
            app.cad_cli_complemento = parametros.cad_cli_complemento;
            app.cad_cli_cidade = parametros.cad_cli_cidade;
            app.cad_cli_sexo = parametros.cad_cli_sexo;

            app.url_app_android = parametros.url_app_android;

            app.mostra_builder = parametros.FN;
            app.is_fn = parametros.FN;
            app.habilitar_contato = parametros.habilitar_contato;
            app.whatsapp_link = parametros.whatsapp_link;
            app.webchat_link = parametros.webchat_link;
            app.telefone_contato = parametros.telefone_contato;
        }
    }

    resetaCooldown() {
        clearInterval(counter);
        router.history.current.matched[0].instances.default.cooldown = 300;
    }

    recuperarSenha(user, cpf) {
        const data = {
            USER: user,
            CPF: cpf,
            ACTION: 'setRecuperarSenha',
        };

        try {
            const hs_web = new HotsiteWeb();
            $.get(`${__SERVER__}/model/login/login.php`, data, (data) => {
                data = JSON.parse(data);
                if (data != '' && data != undefined) {
                    if (data.tipo == 'sucesso') {
                        hs_web.criarToast('5000', 'Sucesso!', data.mensagem, 'fas fa-check', 'green', 'id_sucesso', true, '', 'topCenter');
                    } else {
                        hs_web.resetaCooldown();
                        hs_web.criarToast('5000', 'Erro!', data.mensagem, 'fas fa-exclamation-circle', 'red', 'id_erro', true, '', 'topCenter');
                    }
                    return router.history.current.matched[0].instances.default.loading_senha = false;
                }
                hs_web.resetaCooldown();
                hs_web.criarToast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
                return router.history.current.matched[0].instances.default.loading_senha = false;
            }).fail((e) => {
                hs_web.resetaCooldown();
                hs_web.criarToast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
                return router.history.current.matched[0].instances.default.loading_senha = false;
            });
        } catch (e) {
            new HotsiteWeb().resetaCooldown();
            router.history.current.matched[0].instances.default.loading_senha = false;
            return `Exception Class HotsiteWeb -> encerrarSessaoHotsite() -> ${e}`;
        }
    }

    /** VALIDA BUILDER ACESSES */
    validaAcessoBuilder() {
        const data = {
            ACTION: 'getValidaAcessoBuilder',
        };

        try {
            $.get(`${__SERVER__}/model/login/login.php`, data, (data) => {
                data = JSON.parse(data);

                if (data != '' && data != undefined) {
                    if (data.tipo == 'erro') {
                        return router.replace('/central_assinante_web/');
                    }

                    $(document).ready(() => {
                        app.loading = false;
                        router.history.current.matched[0].instances.default.mostra_builder = true;
                    });
                    return true;
                }

                return router.replace('/central_assinante_web/');
            }).fail((e) => router.replace('/central_assinante_web/'));
        } catch (e) {
            return router.replace('/central_assinante_web/');
        }
    }

    /**
     * Metodo para esconder o Toast com base na classe CSS passada por parametro
     */
    esconderToast(classe = '') {
        if (classe !== '') {
            const toast = document.querySelector(`.${classe}`);

            iziToast.hide({}, toast);

            iziToast.hide({
                transitionOut: 'fadeOutUp',
            }, toast);
        }
    }

    /**
     * Criar Toast Geral
     */
    criarToast(timeout, title, message, icon_text, color, id, toast_once = true, classeCSS = '', position = 'topRight') {
        if (iziToast) {
            iziToast.destroy();
        }
        iziToast.show({
            timeout,
            title,
            message,
            position,
            icon: icon_text,
            // icon: 'material-icons', // comentado pois impedia a utilização do ícone correto para padronização.
            color,
            closeOnEscape: true,
            id,
            toastOnce: toast_once,
            class: classeCSS,
        });
    }

    criarToastPendencia(timeout, title, message, icon_text, color, id, botao_pendencia, url_pendencia) {
        iziToast.show({
            timeout,
            title,
            message,
            position: 'topRight',
            iconText: icon_text,
            // icon: 'material-icons', // comentado pois impedia a utilização do ícone correto para padronização.
            color,
            closeOnEscape: true,
            id,
            toastOnce: true,
            buttons: [
                [`<button><b>${botao_pendencia}</b></button>`, function (instance, toast) {
                    router.replace(ROTA + url_pendencia);
                    ANIMA_ICONE = true;
                    instance.hide({ transitionOut: 'fadeOut' }, toast, 'button');
                    iziToast.destroy();
                }, true],
            ],
        });
    }

    criarToastPendenciaAtendimento(timeout, title, message, icon_text, color, id, botao_pendencia, url_pendencia, id_atendimento_toast, validar_mensagem_lida) {
        iziToast.show({
            timeout,
            title,
            message,
            position: 'topRight',
            iconText: icon_text,
            icon: 'material-icons',
            color,
            closeOnEscape: true,
            id,
            toastOnce: true,
            buttons: [
                [`<button><b>${botao_pendencia}</b></button>`, function (instance, toast) {
                    router.replace(url_pendencia);
                    validar_mensagem_lida(id_atendimento_toast);
                    instance.hide({ transitionOut: 'fadeOut' }, toast, 'button');
                    iziToast.destroy();
                }, true],
            ],
        });
    }

    /**
     * Criar toast de finalização de atendimento
     */
    criarToastFinalizar(id_atendimento) {
        const data = {
            ID_ATENDIMENTO: id_atendimento || 0,
        };

        const finalizar_atendimento = this.finalizarAtendimento;
        const criar_toast = this.criarToast;
        const valida_resize_home = this.validaResizeHome;
        const valida_resize_atendimentos = this.validaResizeAtendimentos;
        const valida_loading_function = this.validaLoading;
        const buscar_atendimentos = this.buscarAtendimentos;
        let mensagem = '';
        const validar_mensagem_lida = this.validarMensagemLida;

        iziToast.info({
            timeout: 99999999999,
            overlay: true,
            class: 'toastFinalizar',
            toastOnce: true,
            id: 'inputs',
            zindex: 999,
            message: 'Por favor, informe um motivo para finalizar o atendimento.',
            position: 'center',
            drag: false,
            backgroundColor: 'white',
            inputs: [
                ['<textarea v-model="mensagem_finalizacao" required rows="4" cols="50">', 'keydown', function (instance, toast, input, e) {
                    mensagem = input.value;
                }],
            ],
            buttons: [
                ['<button><b>Finalizar</b></button>', function (instance, toast) {
                    finalizar_atendimento(id_atendimento, [], SLICE_ATENDIMENTO, mensagem, criar_toast, buscar_atendimentos, valida_resize_home, valida_resize_atendimentos, valida_loading_function, validar_mensagem_lida);
                    instance.hide({ transitionOut: 'fadeOut' }, toast, 'button');
                }, true],
            ],
        });
    }

    /**
     * Criar toast de comprovante de pagamento
     */
    criarToastComprovante(id_receber, recorrente) {
        const data = {
            ID_RECEBER0: id_receber || 0,
        };

        const hs_web = new HotsiteWeb();

        let campos = [];
        if (!recorrente) {
            campos = [
                ['<button><b>Imprimir</b></button>', function (instance, toast) {
                    hs_web.comprovantePagamento(id_receber, 'N');
                    instance.hide({ transitionOut: 'fadeOut' }, toast, 'button');
                }, true],
                ['<button><b>Enviar por E-mail</b></button>', function (instance, toast) {
                    hs_web.comprovantePagamento(id_receber, 'S');
                    instance.hide({ transitionOut: 'fadeOut' }, toast, 'button');
                }, true],
            ];
        } else {
            campos = [['<button><b>Imprimir</b></button>', function (instance, toast) {
                hs_web.comprovantePagamento(id_receber, 'N');
                instance.hide({ transitionOut: 'fadeOut' }, toast, 'button');
            }, true]];
        }

        iziToast.info({
            timeout: 99999999999,
            overlay: true,
            class: 'toastFinalizar',
            toastOnce: true,
            id: 'inputs',
            zindex: 999,
            message: 'O que deseja fazer com seu comprovante?',
            position: 'center',
            drag: false,
            backgroundColor: 'white',
            buttons: campos,
        });
    }

    /** Valida Resize */
    validaResizeHome() {
        router.history.current.matched[0].instances.default.tamanho = $(window).width();
        if ($(window).width() < 481) {
            const faturas = router.history.current.matched[0].instances.default.faturas.notifi_dash;
            const planos = router.history.current.matched[0].instances.default.planos.contratos_dash;
            const atendimentos = router.history.current.matched[0].instances.default.atendimentos.atendimentos_dash;

            if (faturas && faturas.length > 0) {
                for (let x = 0; x < faturas.length; x++) {
                    faturas[x].mostrar_detalhes = true;
                }
            }

            if (planos && planos.length > 0) {
                for (let x = 0; x < planos.length; x++) {
                    planos[x].mostrar_detalhes = true;
                }
            }

            for (let x = 0; x < atendimentos.length; x++) {
                atendimentos[x].mostrar_detalhes = false;
            }

            if (faturas && faturas.length > 0) {
                router.history.current.matched[0].instances.default.faturas.notifi_dash = faturas;
            }
            router.history.current.matched[0].instances.default.planos.contratos_dash = planos;
            router.history.current.matched[0].instances.default.atendimentos.atendimentos_dash = atendimentos;
        } else {
            const faturas = router.history.current.matched[0].instances.default.faturas.notifi_dash;
            const planos = router.history.current.matched[0].instances.default.planos.contratos_dash;
            const atendimentos = router.history.current.matched[0].instances.default.atendimentos.atendimentos_dash;

            if (faturas && faturas.length > 0) {
                for (let x = 0; x < faturas.length; x++) {
                    faturas[x].mostrar_detalhes = true;
                }
            }

            if (planos && planos.length > 0) {
                for (let x = 0; x < planos.length; x++) {
                    planos[x].mostrar_detalhes = true;
                }
            }

            for (let x = 0; x < atendimentos.length; x++) {
                atendimentos[x].mostrar_detalhes = true;
            }

            if (faturas && faturas.length > 0) {
                router.history.current.matched[0].instances.default.faturas.notifi_dash = faturas;
            }

            router.history.current.matched[0].instances.default.planos.contratos_dash = planos;
            router.history.current.matched[0].instances.default.atendimentos.atendimentos_dash = atendimentos;
        }
    }

    validaResizeFaturas() {
        router.history.current.matched[0].instances.default.tamanho = $(window).width();
        if ($(window).width() < 481) {
            const { faturas } = router.history.current.matched[0].instances.default.faturas;

            for (let x = 0; x < faturas.length; x++) {
                faturas[x].mostrar_detalhes = false;
            }

            if (faturas.length > 0) {
                faturas[0].mostrar_detalhes = true;
            }
            router.history.current.matched[0].instances.default.faturas.notifi_dash = faturas;
        } else {
            const faturas = router.history.current.matched[0].instances.default.faturas.notifi_dash;

            for (let x = 0; x < faturas.length; x++) {
                faturas[x].mostrar_detalhes = true;
            }

            router.history.current.matched[0].instances.default.faturas.notifi_dash = faturas;
        }
    }

    validaResizePlanos() {
        router.history.current.matched[0].instances.default.tamanho = $(window).width();
        if ($(window).width() < 767) {
            const { planos } = router.history.current.matched[0].instances.default.planos;

            for (let x = 0; x < planos.length; x++) {
                planos[x].mostrar_detalhes = false;
            }

            if (planos.length > 0) {
                planos[0].mostrar_detalhes = true;
            }

            router.history.current.matched[0].instances.default.planos.contratos_dash = planos;
        } else {
            const planos = router.history.current.matched[0].instances.default.planos.contratos_dash;

            for (let x = 0; x < planos.length; x++) {
                planos[x].mostrar_detalhes = true;
            }

            router.history.current.matched[0].instances.default.planos.contratos_dash = planos;
        }
    }

    validaResizeAtendimentos() {
        router.history.current.matched[0].instances.default.tamanho = $(window).width();
        if ($(window).width() < 768) {
            const { atendimentos } = router.history.current.matched[0].instances.default.atendimentos;

            for (let x = 0; x < atendimentos.length; x++) {
                atendimentos[x].mostrar_detalhes = false;
            }

            if (atendimentos.length > 0) {
                atendimentos[0].mostrar_detalhes = true;
            }
            router.history.current.matched[0].instances.default.atendimentos.atendimentos_dash = atendimentos;
        } else {
            const atendimentos = router.history.current.matched[0].instances.default.atendimentos.atendimentos_dash;

            for (let x = 0; x < atendimentos.length; x++) {
                atendimentos[x].mostrar_detalhes = true;
            }

            router.history.current.matched[0].instances.default.atendimentos.atendimentos_dash = atendimentos;
        }
    }

    validaResizeNotas() {
        router.history.current.matched[0].instances.default.tamanho = $(window).width();
    }

    validaLoading() {
        if (!LOADING_FATURA && !LOADING_FRANQUIA && !LOADING_PLANO && !LOADING_ATENDIMENTO && !LOADING_CONNECTIONS) {
            router.history.current.matched[0].instances.default.is_safari = IS_SAFARI;
            router.history.current.matched[0].instances.default.loading = false;
            router.history.current.matched[0].instances.default.mostrar_atendimento = 'S';

            const hs_web = new HotsiteWeb();
            hs_web.setarPermissaoHome();

            app.loading = false;

            $(document).ready(() => {
                validaActiveClass('#pg_home');
                const hs_web = new HotsiteWeb();

                hs_web.setarPermissaoHome();
            });
        }
    }

    /** PERMISSOES * */
    setarPermissaoHome() {
        $(document).ready(() => {
            router.history.current.matched[0].instances.default.mostrar_franquia = app.pg_consumo;
            router.history.current.matched[0].instances.default.mostrar_fatura = app.pg_fatura;
            router.history.current.matched[0].instances.default.mostrar_plano = app.pg_plano;
            router.history.current.matched[0].instances.default.mostrar_atendimento = app.pg_atendimento;
            router.history.current.matched[0].instances.default.sms_fatura = app.sms_fatura;
            router.history.current.matched[0].instances.default.email_fatura = app.email_fatura;
            router.history.current.matched[0].instances.default.imprime_venda_fatura = app.imprime_venda_fatura;
            router.history.current.matched[0].instances.default.exibir_detalhamento_financeiro = app.pg_exibir_detalhamento_financeiro;
            router.history.current.matched[0].instances.default.mostrar_connections = app.connections;
        });
    }

    setarPermissaoFaturas() {
        $(document).ready(() => {
            router.history.current.matched[0].instances.default.sms_fatura = app.sms_fatura;
            router.history.current.matched[0].instances.default.email_fatura = app.email_fatura;
            router.history.current.matched[0].instances.default.imprime_venda_fatura = app.imprime_venda_fatura;
        });
    }

    setarPermissaoDadosCliente() {
        $(document).ready(() => {
            router.history.current.matched[0].instances.default.cad_cli_fantasia = app.cad_cli_fantasia;
            router.history.current.matched[0].instances.default.cad_cli_cpf = app.cad_cli_cpf;
            router.history.current.matched[0].instances.default.cad_cli_rg = app.cad_cli_rg;
            router.history.current.matched[0].instances.default.cad_cli_data_nascimento = app.cad_cli_data_nascimento;
            router.history.current.matched[0].instances.default.cad_cli_telefone = app.cad_cli_telefone;
            router.history.current.matched[0].instances.default.cad_cli_celular = app.cad_cli_celular;
            router.history.current.matched[0].instances.default.cad_cli_telefone_comercial = app.cad_cli_telefone_comercial;
            router.history.current.matched[0].instances.default.cad_cli_ramal = app.cad_cli_ramal;
            router.history.current.matched[0].instances.default.cad_cli_cep = app.cad_cli_cep;
            router.history.current.matched[0].instances.default.cad_cli_endereco = app.cad_cli_endereco;
            router.history.current.matched[0].instances.default.cad_cli_numero = app.cad_cli_numero;
            router.history.current.matched[0].instances.default.cad_cli_bairro = app.cad_cli_bairro;
            router.history.current.matched[0].instances.default.cad_cli_referencia = app.cad_cli_referencia;
            router.history.current.matched[0].instances.default.cad_cli_complemento = app.cad_cli_complemento;
            router.history.current.matched[0].instances.default.cad_cli_cidade = app.cad_cli_cidade;
            router.history.current.matched[0].instances.default.cad_cli_sexo = app.cad_cli_sexo;
        });
    }

    setFavIcon() {
        const data = {
            ACTION: 'getFavIcon',
        };
        const criar_toast = this.criarToast;

        $.get(`${__SERVER__}/model/login/login.php`, data, (data) => {
            data = JSON.parse(data);

            const docHead = document.getElementsByTagName('head')[0];
            const newLink = document.createElement('link');
            newLink.rel = 'shortcut icon';
            newLink.href = `data:image/png;base64,${data}`;
            docHead.appendChild(newLink);
        }).fail((e) => {
            criar_toast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
        });
    }

    removeCache() {
        if (localStorage.getItem('dados')) {
            localStorage.removeItem('dados');
        }

        if (localStorage.getItem('parametros')) {
            localStorage.removeItem('parametros');
        }

        if (localStorage.getItem('sessao')) {
            localStorage.removeItem('sessao');
        }

        if (localStorage.getItem('manter_conectado')) {
            localStorage.removeItem('manter_conectado');
        }

        if (sessionStorage.getItem('dados')) {
            sessionStorage.removeItem('dados');
        }

        if (sessionStorage.getItem('parametros')) {
            sessionStorage.removeItem('parametros');
        }
        if (sessionStorage.getItem('sessao')) {
            sessionStorage.removeItem('sessao');
        }

        if (sessionStorage.getItem('manter_conectado')) {
            sessionStorage.removeItem('manter_conectado');
        }

        router.replace('/central_assinante_web/login');
    }

    getContratosRecorrentes() {
        const hs_web = new HotsiteWeb();

        const data = {
            ACTION: 'getContratosRecorrentes',
        };

        try {
            $.post(`${__SERVER__}/model/configuracoes/configuracoes.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    data = data;
                }
                if (data != '' && data != undefined) {
                    if (data.tipo == 'sucesso') {
                        router.history.current.matched[0].instances.default.contratos_recorrentes = data.mensagem;
                    } else {
                        hs_web.criarToast('2000', 'Erro!', `${data.mensagem}.`, 'fas fa-exclamation-circle', 'red', 'id_erro', false);
                    }
                }

                return app.loading = false;
            }).fail((e) => {
                hs_web.criarToast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro', false);
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> getContratosRecorrentes() -> ${e}`;
        }
    }

    setCancelarRecorrencias(contratos) {
        const hs_web = new HotsiteWeb();

        const data = {
            CONTRATOS: contratos || [],
            ACTION: 'setCancelarRecorrencias',
        };

        try {
            $.post(`${__SERVER__}/model/configuracoes/configuracoes.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    data = data;
                }
                if (data != '' && data != undefined) {
                    if (data.tipo == 'sucesso') {
                        let contractIds = [];

                        contratos.map((contrato) => {
                            contractIds.push(contrato.id);
                        });

                        contractIds = contractIds.join(',');

                        router.history.current.matched[0].instances.default.contratos_recorrentes = data.mensagem;
                        hs_web.criarToast('4000', 'Sucesso!', `O pagamento recorrente foi cancelado para o(s) contrato(s) ${contractIds}.`, 'fas fa-check', 'green', 'id_sucesso');
                    } else {
                        hs_web.criarToast('4000', 'Erro!', `${data.mensagem}.`, 'fas fa-exclamation-circle', 'red', 'id_erro', false);
                    }
                }

                return app.loading = false;
            }).fail((e) => {
                hs_web.criarToast('5000', 'Erro!', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro', false);
            });
            router.history.current.matched[0].instances.default.contratos_marcados = [];
        } catch (e) {
            return `Exception Class HotsiteWeb -> setCancelarRecorrencias() -> ${e}`;
        }
    }

    montarModalConsultaFatura(fatura) {
        app.consultar_fatura_modal = fatura;
        app.modal_nome = `Fatura ${fatura.id}`;
        $('.main-panel').css('opacity', '0.3');
        $('#modalConsultarFatura').on('hidden.bs.modal', () => {
            if ($('#modalSelecionarBanco').css('display') == 'none') {
                $('.main-panel').css('opacity', '1');
            }
        });

        $('#modalConsultarFatura').on('shown.bs.modal', (e) => {
            $('.topbar , .sidebar, #botaoPagarCreditCard, .botoes_fechar').click((e) => {
                $('#fecharConsultarFatura').click();
            });
        });
    }

    montarModalSelecionarBanco(fatura) {
        app.consultar_fatura_modal = fatura;
        $('.main-panel').css('opacity', '0.3');
        $('#modalSelecionarBanco').on('hidden.bs.modal', () => {
            $('.main-panel').css('opacity', '1');
        });

        $('.topbar .sidebar, #botaoPagarCreditCard, .botoes_fechar').click((e) => {
            $('#fecharModalSelecionarBanco').click();
        });
    }

    copiarCodigoBarrasModal(linha_digitavel) {
        const hs_web = new HotsiteWeb();
        const textArea = document.createElement('input');
        textArea.setAttribute('class', 'codigo_de_barras_hidden');
        linha_digitavel = linha_digitavel.replaceAll(',', '').replaceAll(' ', '');

        textArea.value = linha_digitavel;

        document.getElementById('modalConsultarFatura').appendChild(textArea);

        if (IS_SAFARI === true) {
            const editable = textArea.contentEditable;
            const { readOnly } = textArea;
            textArea.contentEditable = true;
            textArea.readOnly = true;
            const range = document.createRange();
            range.selectNodeContents(textArea);
            const selection = window.getSelection();
            selection.removeAllRanges();
            selection.addRange(range);
            textArea.setSelectionRange(0, 999999);
            textArea.contentEditable = editable;
            textArea.readOnly = readOnly;
        } else {
            textArea.select();
        }

        const successful = document.execCommand('copy');

        if (successful) {
            hs_web.criarToast(4000, 'Sucesso!', 'Código copiado para área de transferência.', 'fas fa-check', 'green', 'id_sucesso');
        } else {
            hs_web.criarToast(4000, 'Erro!', 'Não foi possível copiar o código.', 'fas fa-exclamation-circle', 'red', 'id_erro');
        }
        textArea.parentNode.removeChild(textArea);
    }

    imprimirFaturaComSelecaoDeBanco(dadosReceber, dadosCarteira) {
        this.restoreModalVariavel();

        const data = {
            ACTION: 'alterarCarteiraFatura',
            ID_CONTRATO: dadosReceber.id_contrato,
            ID_CONTRATO_AVULSO: dadosReceber.id_contrato_avulso,
            ID_NOVA_CARTEIRA: dadosCarteira.id,
            ID_RECEBER: dadosReceber.id,
            TIPO_RECEBIMENTO_NOVA_CARTEIRA: dadosCarteira.tipo_recebimento,
            ID_CARTEIRA_ANTIGA: dadosReceber.id_carteira_cobranca,
        };
        const criar_toast = this.criarToast;

        try {
            const mensagemSucesso = dadosCarteira.tipo_recebimento == 'Gateway' ? '30 minutos' : '1 dia';

            $.post(`${__SERVER__}/model/faturas/faturas.php`, data, (data) => {
                data = JSON.parse(data);
                if (data.alterouCarteiraFaturas) {
                    $(document).ready(() => {
                        app.loading = false;
                        router.history.current.matched[0].instances.default.loading = false;
                        app.modal_nome = `Fatura ${dadosReceber.id}`;
                        $('#fecharModalSelecionarBanco').click();
                        this.criarToast('8000', 'Sucesso!', `favor aguardar ${mensagemSucesso} para realizar o pagamento após a seleção do banco da fatura. Ao fechar a impressão a página será recarregada!`, 'fas fa-check', 'green', 'id_sucesso');
                        this.imprimirFatura(dadosReceber.id);
                        $('#modalImpressao').on('hidden.bs.modal', () => {
                            window.location.reload();
                        });
                    });
                    return true;
                }
                criar_toast('5000', 'Ocorreu um erro ao alterar o banco da fatura. Por favor, tente novamente ou entre em contato com o SUPORTE.', 'id_erro', 'fas fa-exclamation-circle', 'red');
                // verificar modificação na base
            }).fail((e) => {
                router.history.current.matched[0].instances.default.cartao_parcelas = [];
                $(document).ready(() => {
                    app.loading = false;
                    router.history.current.matched[0].instances.default.loading = false;
                    validaActiveClass('#pg_fatura');
                });
                criar_toast('5000', 'Ocorreu um erro no servidor. Por favor, entre em contato com o SUPORTE.', 'fas fa-exclamation-circle', 'red', 'id_erro');
            });
        } catch (e) {
            return `Exception Method buscarParcelas() -> ${e}`;
        }
    }

    montarModalEntreEmContato() {
        $('.main-panel').css('opacity', '0.3');
        $('#modalEntreEmContato').on('hidden.bs.modal', () => {
            $('.main-panel').css('opacity', '1');
        });

        $('.topbar .sidebar, .botoes_fechar').click((e) => {
            $('#fecharModalEntreEmContato').click();
        });
    }

    copiarTelefoneContato(telefoneContato) {
        const hs_web = new HotsiteWeb();
        const textArea = document.createElement('input');
        textArea.value = telefoneContato;

        document.getElementById('modalEntreEmContato').appendChild(textArea);

        if (IS_SAFARI) {
            const editable = textArea.contentEditable;
            const { readOnly } = textArea;
            textArea.contentEditable = true;
            textArea.readOnly = true;
            const range = document.createRange();
            range.selectNodeContents(textArea);
            const selection = window.getSelection();
            selection.removeAllRanges();
            selection.addRange(range);
            textArea.setSelectionRange(0, 999999);
            textArea.contentEditable = editable;
            textArea.readOnly = readOnly;
        } else {
            textArea.select();
        }

        const successful = document.execCommand('copy');
        if (successful) {
            hs_web.criarToast(4000, 'Sucesso!', 'Telefone copiado com sucesso!', 'fas fa-check', 'green', 'id_sucesso');
        } else {
            hs_web.criarToast(4000, 'Erro!', 'Não foi possível copiar o telefone.', 'fas fa-exclamation-circle', 'red', 'id_erro');
        }
        textArea.parentNode.removeChild(textArea);
    }

    enviarEmailParaResetarSenha(data) {
        try {
            data.ACTION = 'resetarSenha';
            data.server = __SERVER__;
            const request = $.ajax({
                url: `${__SERVER__}/model/login/login.php`,
                type: 'POST',
                data,
            });
            request.done((response) => {
                const result = JSON.parse(response);
                const hotsiteWeb = new HotsiteWeb();
                if (result.tipo === 'sucesso') {
                    hotsiteWeb.criarToast('5000', 'Sucesso!', result.mensagem, 'fas fa-check', 'green', 'id_sucesso', true, '', 'topCenter');
                } else {
                    hotsiteWeb.criarToast('5000', 'Erro!', result.mensagem, 'fas fa-exclamation-circle', 'red', 'id_erro', true, '', 'topCenter');
                }
                hotsiteWeb.resetaCooldown();
            });
        } catch (error) {
            new HotsiteWeb().resetaCooldown();
            return `Exception Class HotsiteWeb -> enviarEmailParaResetarSenha() -> ${error}`;
        }
    }

    salvarNovaSenha(data) {
        try {
            app.loading = true;
            data.ACTION = 'salvarNovaSenha';
            const request = $.ajax({
                url: `${__SERVER__}/model/login/login.php`,
                type: 'POST',
                data,
            });
            request.done((response) => {
                app.loading = false;
                const result = JSON.parse(response);
                const hotsiteWeb = new HotsiteWeb();
                if (result.tipo === 'sucesso') {
                    hotsiteWeb.criarToast('5000', 'Sucesso!', result.mensagem, 'fas fa-check', 'green', 'id_sucesso');
                    if (localStorage.getItem('trocarSenhaHash')) {
                        localStorage.removeItem('trocarSenhaHash');
                    }
                    setTimeout(() => {
                        location.href = `${__SERVER__}/login`;
                    }, 5000);
                } else {
                    hotsiteWeb.criarToast('5000', 'Erro!', result.mensagem, 'fas fa-exclamation-circle', 'red', 'id_erro');
                    setTimeout(() => {
                        if (result.redirect && result.redirect === 'login') {
                            location.href = `${__SERVER__}/login`;
                        }
                    }, 5000);
                }
            });
        } catch (error) {
            app.loading = false;
            return `Exception Class HotsiteWeb -> salvarNovaSenha() -> ${error}`;
        }
    }

    partiuClubeDeVantagens() {
        const hs_web = new HotsiteWeb();

        const data = {
            ACTION: 'partiuClubeDeVantagens',
        };

        try {
            $.post(`${__SERVER__}/model/dados_cliente/dados_cliente.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    return e;
                }
                if (data != '' && data != undefined) {
                    app.pg_partiu_desconto = data.permissao;
                    if (data.cnpj_cpf && data.id_integracao) {
                        hs_web.gerarLoginAutomaticoPartiu(data.cnpj_cpf, data.id_integracao);
                    }
                }
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> partiuClubeDeVantagens() -> ${e}`;
        }
    }

    gerarLoginAutomaticoPartiu(cnpj_cpf, id_integracao) {
        const hs_web = new HotsiteWeb();

        const data = {
            ACTION: 'gerarLoginAutomaticoPartiu',
            CNPJ_CPF: cnpj_cpf,
            ID_INTEGRACAO: id_integracao,
        };

        try {
            $.post(`${__SERVER__}/model/dados_cliente/dados_cliente.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    return e;
                }
                if (data != '' && data != undefined) {
                    app.pg_partiu_desconto_link = data.link;
                }
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> gerarLoginAutomaticoPartiu() -> ${e}`;
        }
    }

    getAppURL() {
        const data = {
            ACTION: 'getAppURL',
        };

        $.get(`${__SERVER__}/model/login/login.php`, data, (data) => {
            data = JSON.parse(data);
            verificarAppEExecutarOuRedirecionar(data[0].mensagem);
        });
    }

    callSvaIntegration() {
        const hs_web = new HotsiteWeb();
        const data = {
            ACTION: 'callSvaIntegration',
        };
        try {
            $.post(`${__SERVER__}/model/dados_cliente/dados_cliente.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    return e;
                }
                app.sva_permission = data.permission;
                if (data.permission === 'S') {
                    app.svaPlatform = data.integration.plataforma;
                    app.svaUsername = data.contract.sva_usuarios_username;
                    app.svaExternalId = data.integration.external_id;
                    app.svaIntegrationId = data.contract.sva_configuracoes_id;
                }
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> callSvaIntegration() -> ${e}`;
        }
    }

    generateLinkSva(svaData) {
        const hs_web = new HotsiteWeb();

        const data = {
            ACTION: 'generateLinkSva',
            SVA_DATA: svaData,
        };

        try {
            $.post(`${__SERVER__}/model/dados_cliente/dados_cliente.php`, data, (data) => {
                try {
                    data = JSON.parse(data);
                    if (data != '' && data != undefined) {
                        window.open(data.message.link, '_blank');
                    }
                } catch (e) {
                    return e;
                }
            });
        } catch (e) {
            return `Exception Class HotsiteWeb -> generateLinkSva() -> ${e}`;
        }
    }
}
