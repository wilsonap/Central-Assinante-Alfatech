buscarDadosClienteHTML();

var funcoes_dados_cliente = {
    alterarSenha(senha_atual, senha_nova, senha_nova_repetir) {
        var hs_web = new HotsiteWeb();
        if(!this.senha_forte){
            hs_web.criarToast('5000', 'Senha fraca!', 'Uma senha forte deve corter ao menos 8 caracteres, incluindo letras maiúsculas, minúsculas e números', 'fas fa-exclamation-circle', 'red', 'id_erro');
            return;
        }
        this.atualizando_dados_senha = true;
        this.atualizar_senha = true;
        hs_web.alterarSenha(senha_atual, senha_nova, senha_nova_repetir);
    },
    setSenhaAtualParaMD5(senha) {
        let hotsiteWeb = new HotsiteWeb();
        hotsiteWeb.setSenhaAtualParaMD5(senha);
    },
    forcaSenha() {
        $('#senha').strength({
            strengthClass: 'strength',
            strengthMeterClass: 'strength_meter',
            strengthButtonClass: 'button_strength'
        });
    },
    validaSenhaForca(senha){
        $('#senha').strength({
            strengthClass: 'strength',
            strengthMeterClass: 'strength_meter',
            strengthButtonClass: 'button_strength'
        });
        let hasNumber= /(?=.*[0-9])/
        let isLong= /(?=.{8,})/
        let hasUpper= /(?=.*[A-Z])/
        let hasLower= /(?=.*[a-z])/
        this.senha_forte = hasNumber.test(senha) && isLong.test(senha) && hasUpper.test(senha) && hasLower.test(senha);
    },
    habilitarDesabilitarAcessoAutomatico() {
        var hs_web = new HotsiteWeb();
        var acesso_automatico = $("#acesso_automatico").is(":checked");

        app.loading = true;
        hs_web.habilitarDesabilitarAcessoAutomatico(acesso_automatico);
    },
    validaEstadoSelecionado(estado) {
        var hs_web = new HotsiteWeb();

        if (estado) {
            app.loading = true;
            return hs_web.getCidades(estado);
        }

        return;
    },
    filtrarCidades(array) {
        this.mostra_autocomplete = true;
        INDEX_AUTOCOMPLETE = -1;
        var app = this;

        return array.filter(function (array) {
            let regex = new RegExp('(' + app.dados_cliente.cidade_nome + ')', 'i');
            return array.nome.match(regex);
        })
    },
    setaParaCima() {
        var total_registros = $("div.autocomplete div.options").length;

        if (INDEX_AUTOCOMPLETE > 0) {
            INDEX_AUTOCOMPLETE--;

        } else {
            INDEX_AUTOCOMPLETE = total_registros - 1;
        }

        var obj_id = $("div.autocomplete div.options")[INDEX_AUTOCOMPLETE].id;
        var el = document.getElementById(obj_id);
        var scroll = $('div.autocomplete')[0];
        scroll.scrollTop = el.offsetTop;
        el.focus();
    },
    setaParaBaixo() {
        var total_registros = $("div.autocomplete div.options").length;

        if (INDEX_AUTOCOMPLETE < total_registros - 1) {
            INDEX_AUTOCOMPLETE++;
        } else {
            INDEX_AUTOCOMPLETE = 0;
        }

        var obj_id = $("div.autocomplete div.options")[INDEX_AUTOCOMPLETE].id;
        var el = document.getElementById(obj_id);
        var scroll = $('div.autocomplete')[0];
        scroll.scrollTop = el.offsetTop;
        el.focus();
    },
    setas(cidade = {}) {
        if (event.keyCode === 40) {
            this.setaParaBaixo();
        } else if (event.keyCode === 38) {
            this.setaParaCima();
        } else if (event.keyCode === 13) {
            this.selecionarCidades(cidade);
        }
    },
    selecionarCidades(item) {
        this.dados_cliente.cidade_id = item.id;
        this.dados_cliente.cidade_nome = item.nome;
        this.mostra_autocomplete = false;

        $("input[name='cidades'].cidades").focus();
    },
    cancelar(editar) {
        if (!editar) {
            app.loading = true;
            var hs_web = new HotsiteWeb();
            hs_web.buscarDadosCliente();
        }

        this.atualizando_dados = editar;
    },
    salvar() {
        app.loading = true;
        var dados = this.dados_cliente;

        if (!dados.cidade_nome) {
            dados.cidade_id = 0;
        }

        var hs_web = new HotsiteWeb();
        return hs_web.solicitarAtualizacaoCadastro(dados);
    },
    confirmarAlteracao() {
        if (confirm('Atenção! Verifique os dados antes de prosseguir com a solicitação de alteração cadastral. Uma vez feita a solicitação, somente será possível alterar novamente após a análise pelo setor responsável.')) {
            this.salvar();
        }
    }
};

function buscarDadosClienteHTML() {
    try {
        $.ajax({
            url: __SERVER__ + "/view/dados_cliente/dados_cliente.vue",
            type: "GET",
            data: "",
            dataType: 'html'
        }).done(function (resposta) {
            __ROTAS__.push({
                path: '/central_assinante_web/dados_cliente',
                name: 'dadoscliente',
                component: {
                    template: resposta,
                    data() {
                        return {
                            loading: true,
                            atualizando_dados: false,
                            atualizando_dados_senha: false,
                            atualizar_senha: false,
                            senha_forte:false,
                            mostrar_privacidade: false,
                            mostra_autocomplete: false,
                            mostrar_botao_editar_dados: true,
                            dados_cliente_padrao: {},
                            dados_cliente: {},
                            paises: {},
                            estados: {},
                            cidades: {},
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
                            cad_cli_sexo: 'E'
                        };
                    },
                    methods: funcoes_dados_cliente
                }
            });
            PAGE_DADOS_CLIENTE = true;
            verificaPages();
        });
    } catch (e) {
        console.log("Error Message -> " + e);
    }
}