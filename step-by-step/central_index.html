<html lang="pt">
<head>
    <meta http-equiv="Content-Security-Policy"
          content="style-src 'self' * 'unsafe-inline';img-src 'self' * data: 'unsafe-inline';script-src 'self' * 'unsafe-eval' 'unsafe-inline'; object-src 'self' data: * 'unsafe-eval';">
    <meta name="msapplication-tap-highlight" content="no">
    <meta name="viewport"
          content="user-scalable=no, initial-scale=1, maximum-scale=1, minimum-scale=1, width=device-width">

    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <title>Central do Assinante</title>
    <!--<link rel="shortcut icon" href="assets/img/fav.ico">-->
    <link rel="shortcut icon" type="image/x-icon" href="data:image/x-icon;,">
    <!-- Bootstrap -->
    <link href="/central_assinante_web/assets/lib/bootstrap_v3.3.4/css/bootstrap.min.css" rel="stylesheet"/>
    <!-- Notificações -->
    <link href="/central_assinante_web/assets/lib/iziToast_v1.3.0/css/iziToast.min.css" rel="stylesheet"/>
    <!-- Animação -->
    <link href="/central_assinante_web/assets/lib/animate_v3.5.2/css/animate.css" type="text/css" rel="stylesheet"
          id="animate_link">
    <!-- Tooltip -->
    <link rel="stylesheet" href="/central_assinante_web/assets/lib/VueDirectiveTooltip_v1.4.5/css/index.min.css">
    <!--  Main    -->
    <link href="/central_assinante_web/assets/css/main.css" rel="stylesheet"/>
    <!--  Home   -->
    <link href="/central_assinante_web/assets/css/home/home.css" rel="stylesheet"/>
    <!--  Atendimento   -->
    <link href="/central_assinante_web/assets/css/atendimento/atendimento.css" rel="stylesheet"/>
    <!--  Connections   -->
    <link href="/central_assinante_web/assets/css/connections/connections.css" rel="stylesheet"/>
    <!--  Dados Cliente   -->
    <link href="/central_assinante_web/assets/css/dados_cliente/dados_cliente.css" rel="stylesheet"/>
    <!--login-->
    <link href="/central_assinante_web/assets/css/login/login.css" rel="stylesheet"/>
    <!--speed test-->
    <link href="/central_assinante_web/assets/css/speed_test/speed_test.css" rel="stylesheet"/>
    <!--relatorios-->
    <link href="/central_assinante_web/assets/css/relatorios/relatorios.css" rel="stylesheet"/>
    <!--relatorios-->
    <link href="/central_assinante_web/assets/css/configuracoes/configuracoes.css" rel="stylesheet"/>
    <!--Cartão de crédito-->
    <script src="/central_assinante_web/assets/lib/card-master/js/card.js"></script>
    <!-- Fontes e icones -->
    <link href="/central_assinante_web/assets/lib/Roboto/roboto.css" rel="stylesheet">
    <link href="/central_assinante_web/assets/lib/fontawesome-5.15.4-web/css/all.css" rel="stylesheet" integrity="sha12345blahblahblahblahblah" crossorigin="anonymous"/>
    <link href="/central_assinante_web/assets/lib/MaterialIcons/material-icons.css" rel="stylesheet">
    <!--Ações-->
    <link href="/central_assinante_web/assets/css/consultar_fatura/consultar_fatura.css" rel="stylesheet"/>
    <!--PIX-->
    <link href="/central_assinante_web/assets/css/pix/pix.css" rel="stylesheet"/>
    <!--Suspensao temporaria-->
    <link href="/central_assinante_web/assets/css/suspensao_contrato/suspensao_contrato.css" rel="stylesheet"/>

    <script type="text/javascript" src="https://js.iugu.com/v2"></script>
</head>
<body>

<div class="wrapper cordova_app" id="app">
    <div class="topbar animated slideInDown" v-if="!login">
        <router-link class="cursor-pointer" to="/central_assinante_web/">
            <div class="logo" :style="{ backgroundImage: 'url(\'' + logo_base + '\')' }">
            </div>
        </router-link>
        <div class="usuario">
            <div class="nav-item dropdown">
                <a class="nav-link cursor-pointer d-flex align-items-center" id="navbarDropdownMenuLink"
                   data-toggle="dropdown" aria-haspopup="true">
                    <div class="avatar">
                        <div class="inicial text-gray">
                            {{cliente_nome.slice(0,1)}}
                        </div>
                    </div>
                    <div class="nome">
                        {{cliente_nome}}
                    </div>
                    <i class="material-icons">arrow_drop_down</i>
                </a>
                <div class="dropdown-menu dropdown-menu-right" aria-labelledby="navbarDropdownMenuLink">
                    <router-link id="pg_meus_dados" to="/central_assinante_web/dados_cliente" class="dropdown-item"
                                 onclick="validaActiveClass('#pg_meus_dados')">
                        <div class="item-meus-dados">
                            <i class="material-icons">person</i>
                            Meus Dados
                        </div>
                    </router-link>
                    <router-link id="pg_config" to="/central_assinante_web/configuracoes" class="dropdown-item"
                                 onclick="validaActiveClass('#pg_config')">
                        <div class="item-meus-dados">
                            <i class="material-icons">payment</i>
                            Recorrências
                        </div>
                    </router-link>
                    <router-link id="pg_speedtest" to="/central_assinante_web/speed_test" class="dropdown-item"
                                  v-if="showSpeedTest && app.oner != 1"
                                 v-on:click=function(){chamarFuncaoHotsiteWeb('getDadosServidor')}
                                 onclick="validaActiveClass('#pg_speedtest')">
                        <div class="item-meus-dados">
                            <i class="material-icons">network_check</i>
                            Speed Test
                        </div>
                    </router-link>

                    <a v-if="app.is_fn && app.url_generate_app" id="pg_generate_app"
                       class="dropdown-item"
                       onclick="generateApp('#pg_generate_app')">
                        <div class="item-meus-dados">
                            <i class="material-icons">phone_android</i>
                            Gerar App
                        </div>
                    </a>

                    <a v-if="!cordova_app && url_app_android != ''" id="pg_app_download"
                       :href="url_app_android"
                       target="_blank" class="dropdown-item"
                       onclick="validaActiveClass('#pg_app_download')">
                        <div class="item-meus-dados">
                            <i class="material-icons">cloud_download</i>
                            Baixar App
                        </div>
                    </a>

                    <hr class="hr-menu-dropdown"/>
                    <div class="item-meus-dados cursor-pointer dropdown-item" v-on:click="encerrarSessao()"
                         onclick="validaActiveClass('#pg_home')">
                        <i class="material-icons">exit_to_app</i>
                        Sair
                    </div>
                </div>
            </div>
            <button v-on:click="getDadosServidor" type="button" class="navbar-toggle" data-toggle="collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
        </div>
    </div>

    <div class="sidebar animated slideInLeft" v-bind:class="{maximizado: !maximizado, minimizado: maximizado}"
         v-if="!login">
        <div class="sidebar-wrapper" v-bind:class="{maximizado: !maximizado, minimizado: maximizado}">
            <ul class="nav">
                <li class="menu-item perfil">
                    <div class="nav-item dropdown">
                        <div class="d-flex align-items-center">
                            <router-link class="link-perfil d-flex align-items-center"
                                         to="/central_assinante_web/dados_cliente"
                                         onclick="validaActiveClass('#pg_dados')">
                                <div class="avatar">
                                    <div class="inicial text-gray">
                                        {{cliente_nome.slice(0,1)}}
                                    </div>
                                </div>
                                <div class="nome">
                                    {{cliente_nome}}
                                    <p class="ver-infos">Ver dados</p>
                                </div>
                            </router-link>
                            <div class="cursor-pointer" v-on:click="encerrarSessao()"
                                 onclick="validaActiveClass('#pg_home')">
                                <i class="sair material-icons">exit_to_app</i>
                            </div>
                        </div>
                    </div>
                </li>
                <hr v-if="!cordova_app && url_app_android != ''" class="download_android"/>
                <li v-if="!cordova_app && url_app_android != ''" class="download_android">
                    <a :href="url_app_android" target="_blank">
                        <i class="material-icons">cloud_download</i>
                        <p>Baixar App</p>
                    </a>
                </li>
                <hr/>
                <li class="menu-item">
                    <router-link id="pg_home" onclick="validaActiveClass(this)" to="/central_assinante_web/"
                                 v-tooltip.right="maximizado ? {content: 'Início', class: 'tooltip_padrao branco', delay: 1} : {class: 'tooltip_disabled'}">
                        <i class="material-icons">home</i>
                        <p>Início</p>
                    </router-link>
                </li>
                <li class="menu-item" v-if="pg_fatura == 'S'">
                    <router-link id="pg_fatura" onclick="validaActiveClass(this)" to="/central_assinante_web/faturas"
                                 v-tooltip.right="maximizado ? {content: 'Faturas', class: 'tooltip_padrao branco', delay: 1} : {class: 'tooltip_disabled'}">
                        <i class="material-icons">attach_money</i>
                        <p>Faturas</p>
                    </router-link>
                </li>
                <li class="menu-item" v-if="pg_plano == 'S'">
                    <router-link id="pg_plano" onclick="validaActiveClass(this)" to="/central_assinante_web/planos"
                                 v-tooltip.right="maximizado ? {content: 'Contratos', class: 'tooltip_padrao branco', delay: 1} : {class: 'tooltip_disabled'}">
                        <i class="material-icons">receipt</i>
                        <p>Contratos</p>
                    </router-link>
                </li>
                <li class="menu-item" v-if="pg_nota == 'S'">
                    <router-link id="pg_nota" onclick="validaActiveClass(this)" to="/central_assinante_web/notas"
                                 v-tooltip.right="maximizado ? {content: 'Notas', class: 'tooltip_padrao branco', delay: 1} : {class: 'tooltip_disabled'}">
                        <i class="material-icons">note</i>
                        <p>Notas</p>
                    </router-link>
                </li>
                <li class="menu-item" v-if="pg_consumo == 'S' && app.oner != 1">
                    <router-link id="pg_consumo" onclick="validaActiveClass(this)" to="/central_assinante_web/consumos"
                                 v-tooltip.right="maximizado ? {content: 'Consumos', class: 'tooltip_padrao branco', delay: 1} : {class: 'tooltip_disabled'}">
                        <i class="material-icons">insert_chart</i>
                        <p>Consumo</p>
                    </router-link>
                </li>
                <hr class="download_android"/>

                <li class="menu-item not-mobile">
                    <router-link id="pg_relatorios" onclick="validaActiveClass(this)"
                                 to="/central_assinante_web/relatorios"
                                 v-tooltip.right="maximizado ? {content: 'Relatórios', class: 'tooltip_padrao branco', delay: 1} : {class: 'tooltip_disabled'}">
                        <i class="material-icons">library_books</i>
                        <p>Relatórios</p>
                    </router-link>
                </li>
                <li class="menu-item" v-if="pg_connections == 'S' && app.oner != 1">
                    <router-link id="pg_connections" onclick="validaActiveClass(this)" to="/central_assinante_web/connections"
                                 v-tooltip.right="maximizado ? {content: 'Conexões', class: 'tooltip_padrao branco', delay: 1} : {class: 'tooltip_disabled'}">
                        <i class="material-icons">router</i>
                        <p>Conexões</p>
                    </router-link>
                </li>

                <hr class="custom-hr"/>
                <li class="menu-item" v-if="pg_atendimento == 'S'">
                    <router-link id="pg_atendimento" onclick="validaActiveClass(this)"
                                 to="/central_assinante_web/atendimentos"
                                 v-tooltip.right="maximizado ? {content: 'Atendimentos', class: 'tooltip_padrao branco', delay: 1} : {class: 'tooltip_disabled'}">
                        <i class="material-icons">headset_mic</i>
                        <p>Atendimento</p>
                    </router-link>
                </li>

                <li class="menu-item" v-if="pg_partiu_desconto === 'S'">
                    <hr class="custom-hr"/>
                    <a
                            class="cursor-pointer link-no-mobile"
                            target="_blank"
                            :href="pg_partiu_desconto_link"
                            v-tooltip.right="maximizado ? {content: 'Partiu Descontos !', class: 'tooltip_padrao branco', delay: 1} : {class: 'tooltip_disabled'}"
                    >
                        <i class="fas fa-tag"></i>
                        <p>Partiu Descontos !</p>
                    </a>
                </li>


                <li class="menu-item" v-if="sva_permission === 'S'">
                    <hr class="custom-hr"/>
                    <a
                            class="cursor-pointer link-no-mobile"
                            target="_blank"
                            v-tooltip.right="maximizado ? {content: 'Acessar Benefícios Campsoft !', class: 'tooltip_padrao branco', delay: 1} : {class: 'tooltip_disabled'}"
                            v-on:click="chamarFuncaoHotsiteWeb('generateLinkSva', {'platform': app.svaPlatform,'username': app.svaUsername,'external_id': app.svaExternalId,'integrationId': app.svaIntegrationId,})"
                    >
                        <i class="fas fa-tag"></i>
                        <p>Acessar Benefícios Campsoft</p>
                    </a>
                </li>

                <li class="no-mobile">
                    <a
                            class="cursor-pointer link-no-mobile"
                            target="_blank" v-if="whatsapp_link && habilitar_contato === 'S'" v-on:click="openLink({'link':whatsapp_link})"
                            v-tooltip.right="maximizado ? {content: 'Chame pelo WhatsApp', class: 'tooltip_padrao branco', delay: 1} : {class: 'tooltip_disabled'}"
                    >
                        <i class="fab fa-whatsapp"></i>
                        <p>Chamar no whatsapp</p>
                    </a>
                </li>
                <li class="no-mobile">
                    <a
                            class="cursor-pointer link-no-mobile"
                            v-if="webchat_link && habilitar_contato === 'S'" v-on:click="openLink({'link':webchat_link})"
                            v-tooltip.right="maximizado ? {content: 'Chame pelo chat', class: 'tooltip_padrao branco', delay: 1} : {class: 'tooltip_disabled'}"
                    >
                        <i class="fas fa-comment-alt"></i>
                        <p>Contato via chat</p>
                    </a>
                </li>
                <li class="no-mobile">
                    <a class="cursor-pointer"
                       v-if="telefone_contato && habilitar_contato === 'S'"
                       v-on:click="() => {
                            if(typeof window.ReactNativeWebView != 'undefined'){
                                postMessageMobile('call',telefone_contato)
                            }else{
                                window.location.href = 'tel:'+telefone_contato
                            }
                       }"
                       v-tooltip.right="{content: 'Ligar ', class: 'tooltip_padrao', delay: 1}">
                        <i class="fas fa-phone-alt"></i>
                        <p>Ligar</p>
                    </a>
                </li>

                <li class="menu-item entre-em-contato-li"  v-if="habilitar_contato === 'S'">
                    <a
                            class="cursor-pointer"
                            data-toggle="modal"
                            data-target="#modalEntreEmContato"
                            v-on:click=" montarModalEntreEmContato()"
                            v-tooltip.right="maximizado ? {content: 'Entre em contato', class: 'tooltip_padrao branco', delay: 1} : {class: 'tooltip_disabled'}"
                    >
                        <i class="material-icons text-gray icon-hover">forum</i>
                        <p>Entre em contato</p>
                    </a>
                </li>

                <hr class="download_android"/>

                <li class="menu-item no-mobile">
                    <router-link id="pg_relatorios" onclick="validaActiveClass(this)"
                                 to="/central_assinante_web/relatorios"
                                 v-tooltip.right="maximizado ? {content: 'Relatórios', class: 'tooltip_padrao branco', delay: 1} : {class: 'tooltip_disabled'}">
                        <i class="material-icons">library_books</i>
                        <p>Relatórios</p>
                    </router-link>
                </li>

                <li class="menu-item only-mobile">
                    <router-link id="pg_config" onclick="validaActiveClass(this)"
                                 to="/central_assinante_web/configuracoes"
                                 v-tooltip.right="maximizado ? {content: 'Configurações', class: 'tooltip_padrao branco', delay: 1} : {class: 'tooltip_disabled'}">
                        <i class="material-icons">payment</i>
                        <p>Recorrências</p>
                    </router-link>
                </li>
                <li class="menu-item only-mobile">
                    <router-link id="pg_speedtest" onclick="validaActiveClass(this)"
                                 v-if="showSpeedTest && app.oner != 1"
                                 to="/central_assinante_web/speed_test"
                                 v-tooltip.right="maximizado ? {content: 'Speed Test', class: 'tooltip_padrao branco', delay: 1} : {class: 'tooltip_disabled'}">
                        <i class="material-icons">network_check</i>
                        <p>Speed Test</p>
                    </router-link>
                </li>
            </ul>
            <div class="col-md-12 text-center">
                <button class="padding-top-20px text-center"
                        v-bind:class="{maximizar: maximizado, minimizar: !maximizado}"
                        v-on:click="maximizado = !maximizado">
                    <i class="material-icons gesture-icon" v-if="maximizado"
                       v-tooltip.right="{content: 'Expandir Menu', class: 'tooltip_padrao branco', delay: 1}">chevron_right</i>
                    <i class="material-icons gesture-icon" v-else
                       v-tooltip.right="{content: 'Minimizar Menu', class: 'tooltip_padrao branco', delay: 1}">chevron_left</i>
                </button>
            </div>
        </div>
    </div>

    <router-view class="view-rotas" v-bind:class="{maximizado: maximizado, minimizado: !maximizado}"></router-view>
    <div class="modal fade" id="modalPix" tabindex="-1" role="dialog"
         aria-labelledby="gridSystemModalLabel">
        <div class="modal-dialog embeded-style" role="document">
            <div class="modal-content">
                <div class="modal-header pix-header">
                    <button id="botaoFecharModalPix" type="button" class="close" data-dismiss="modal" aria-label="Close" v-on:click="location.reload()"><span
                            aria-hidden="true">&times;</span></button>
                    <img src="assets/img/logo_pix.png" id="logoIxcPix">
                </div>
                <div class="modal-body pix-body">
                    <div class="row" id="dadosPix">
                        <div class="loader-modal" id="pixLoading">
                            Carregando informações do PIX...
                            <div>
                                <span></span>
                                <span></span>
                                <span></span>
                            </div>
                        </div>
                        <div id="headerPix"></div>
                        <div id="qrCodePix"></div>
                        <div id="qrCodePixCopiaECola"></div>
                    </div>
                </div>
                <div class="modal-footer">
                    <div id="pixFooter"></div>
                </div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="modalSuspensao" tabindex="-1" role="dialog"
         aria-labelledby="gridSystemModalLabel">
        <div class="modal-dialog embeded-style" role="document">
            <div class="modal-content">
                <div class="modal-header-suspensao">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                            aria-hidden="true">&times;</span></button>
                    <i class="fas fa-pause icone-suspensao"></i>
                    <h3 class="titulo-suspensao">Suspensão contrato</h3>
                    <p class="descricao-suspensao">Selecione o período desejado e fique atento as informações.</p>
                </div>
                <div class="suspensao-body">
                    <p>Por favor, selecione os dias da suspensão temporária do contrato abaixo.</p>
                    <div class="col-md-4">
                        <div class="group">
                            <input required type="text"
                                   v-mask="['##/##/####']"
                                   :class="{'used':suspensao_data_inicial}"
                                   v-model="suspensao_data_inicial"
                                   :disabled="true"
                            >
                            <label>Data de início</label>
                        </div>
                    </div>
                    <div class="col-md-1">
                        <div class="group">
                            <br>
                            <span>Até</span>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="group">
                            <input required type="text"
                                   v-mask="['##/##/####']"
                                   :class="{'used':suspensao_data_final}"
                                   id="inputDataFinalSuspensao"
                                   @blur="setDataFinalSuspensao()"
                            >
                            <label>Data de término</label>
                        </div>
                    </div>
                    <br>
                    <div>
                        <a class="cursor-pointer"
                           data-toggle="modal"
                           data-target="#modalConcluirSuspensao"
                        >
                            <input type="button" class="botao-suspensao" value="Avançar"
                                   v-on:click="fecharModalSuspensao(intervalo_entre_suspensao, prolonga_fidelidade_tempo_suspensao, tempo_min_suspensao)"/>
                        </a>
                    </div>
                </div>
                <div class="modal-footer">

                </div>
            </div>
        </div>
    </div>
    <div class="modal fade" id="modalConcluirSuspensao" tabindex="-1" role="dialog"
         aria-labelledby="gridSystemModalLabel">
        <div class="modal-dialog embeded-style" role="document">
            <div class="modal-content">
                <div class="modal-header-suspensao">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                            aria-hidden="true">&times;</span></button>
                    <i class="fas fa-pause icone-suspensao"></i>
                    <h3 class="titulo-suspensao">Suspensão contrato</h3>
                    <p class="descricao-suspensao">Leia o aviso com atenção.</p>
                </div>
                <div>
                    <div class="body-espaco">
                        <span></span>
                    </div>
                    <div class="body-concluirSuspensao">
                        <div>
                            <h6><b>ATENÇÃO:</b></h6>
                        </div>
                        <div class="aviso-suspensao">
                            <p>Você pode remover a suspensão temporaria do contrato a qualquer momento, mas <strong>lembre-se:</strong>
                                se você remover antes do dia <strong id="tempoMinSuspensao"></strong>,
                                irá pagar o boleto de forma integral.</p>
                            <p>Você só poderá suspender temporariamente o contrato novamente em <strong
                                    id="intervaloEntreSuspensao"></strong>
                                meses após
                                acabar esta suspensão.</p>

                            <p id="prolongaFidelidadeTempoSuspensao"></p>
                            <p><strong>ATENÇÃO:</strong> sua internet será desativada no momento em que clicar no botão
                                abaixo.
                            </p>
                        </div>
                    </div>
                    <div>
                        <a class="cursor-pointer"
                           data-toggle="modal"
                           v-on:click="chamarFuncaoHotsiteWeb('suspenderContrato',{'id_contrato' : id_contrato, 'data_inicial_suspensao' : suspensao_data_inicial, 'data_final_suspensao' : suspensao_data_final})"
                           v-tooltip.auto="{content: 'Suspender contrato', class: 'tooltip_padrao', delay: 1}"
                        >
                            <button class="botao-concluirSuspensao"><strong>ESTOU CIENTE E QUERO SUSPENDER</strong></button>
                        </a>
                    </div>
                    <div class="modal-footer"></div>
                </div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="modalRemoveSuspensao" tabindex="-1" role="dialog"
         aria-labelledby="gridSystemModalLabel">
        <div class="modal-dialog embeded-style" role="document">
            <div class="modal-content">
                <div class="modal-header-suspensao">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                            aria-hidden="true">&times;</span></button>
                    <i class="fas fa-play icone-suspensao"></i>
                    <h3 class="RemoverSuspensao-titulo">Remover suspensão</h3>
                </div>

                <div class="removerSuspensao-body">
                    <div class="removerSuspensao-aviso">
                        <div>
                            <h6><b>ATENÇÃO:</b></h6>
                        </div>
                        <div class="aviso-suspensao">
                            <p id="tempoMin"></p>
                        </div>
                    </div>
                    <br>

                    <div class="removerSuspensao-PosicaoBotao">
                        <a class="cursor-pointer"
                           data-toggle="modal"
                           data-target="#modalRemoveSuspensaoConcluir"
                        >
                            <input type="button" class="removerSuspensao-botaoAvancar" value="Avançar"
                                   v-on:click="fecharModalRemoverSuspensao(data_inicial, data_final, total_dias_suspensao)"/>
                        </a>
                    </div>

                </div>
                <div class="modal-footer"></div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="modalRemoveSuspensaoConcluir" tabindex="-1" role="dialog"
         aria-labelledby="gridSystemModalLabel">
        <div class="modal-dialog embeded-style" role="document">
            <div class="modal-content">
                <div class="modal-header-suspensao">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                            aria-hidden="true">&times;</span></button>
                    <i class="fas fa-play icone-suspensao"></i>
                    <h3 class="RemoverSuspensao-titulo">Remover suspensão</h3>
                </div>


                <div class="removerSuspensao-body">
                    <p>Você está removendo a suspensão temporária do contrato, confirme as informações a seguir:</p>
                    <div class="removerSuspensao-datas">
                        <div class="col-md-5">
                            <div class="group">
                                <input required type="text"
                                       v-mask="['##/##/####']"
                                       :class="{'used':suspensao_data_inicial}"
                                       v-model="suspensao_data_inicial"
                                       :disabled="true"
                                >
                                <label>Data de início</label>
                            </div>
                        </div>
                        <div class="col-md-1">
                            <div class="group">
                                <br>
                                <span>Até</span>
                            </div>
                        </div>
                        <div class="col-md-5">
                            <div class="group">
                                <input required type="text"
                                       v-mask="['##/##/####']"
                                       :class="{'used':suspensao_data_final}"
                                       v-model="suspensao_data_final"
                                       :disabled="true"
                                >
                                <label>Data de término</label>
                            </div>
                        </div>
                    </div>


                    <div>
                        <p>Total de dias suspenso: <strong id="totalDiasSuspensao"></strong></p>
                    </div>

                    <div class="removerSuspensao-aviso">
                        <div>
                            <h6><b>ATENÇÃO:</b></h6>
                        </div>
                        <div class="aviso-suspensao">
                            <p id="valorProporcional"></p>
                        </div>
                    </div>
                    <br>

                    <div class="removerSuspensao-PosicaoBotaoConcluir">
                        <a class="cursor-pointer"
                           data-toggle="modal"
                           v-on:click="chamarFuncaoHotsiteWeb('removerSuspensaoContrato',{'id_contrato' : id_contrato})"
                           v-tooltip.auto="{content: 'Remover suspensão do contrato', class: 'tooltip_padrao', delay: 1}"
                        >
                            <button class="removerSuspensao-botao"><strong>CONFIRMAR REMOÇÃO</strong>
                            </button>
                        </a>
                    </div>
                </div>


                <div class="modal-footer"></div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="modalImpressao" tabindex="-1" role="dialog"
         aria-labelledby="gridSystemModalLabel" v-if="!cordova_app">
        <div class="modal-dialog embeded-style modal-lg modal-xl" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" id="modalImpressaoClose" class="close" data-dismiss="modal" aria-label="Close"><span
                            aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title" id="gridSystemModalLabel">{{modal_nome}}</h4>
                </div>
                <div class="modal-body">
                    <div class="row">
                        <div class="col-md-12 scroll" v-if="modal_url">
                            <embed v-if="!pdf_fatura_nota" :src="'data:application/pdf;base64,'+modal_url" width="100%"
                                   height="500px"
                                   type="application/pdf"/>
                        </div>

                        <a class="cursor-pointer center" target="_blank" v-if="link_fatura" :href="link_fatura"><i
                                class="material-icons text-gray icon-hover">link</i> Abrir Arquivo</a>

                        <a class="cursor-pointer center" v-if="pdf_fatura_nota" :href="pdf_fatura_nota" download><i
                                class="material-icons text-gray icon-hover">print</i> Baixar Arquivo</a>

                    </div>
                </div>
                <div class="modal-footer" v-if="loading_modal && !pdf_fatura_nota && !link_fatura">
                    <div class="loader-modal">
                        Carregando documento
                        <div>
                            <span></span>
                            <span></span>
                            <span></span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="modal fade" id="modalConsultarFatura" tabindex="-1" role="dialog"
         aria-labelledby="gridSystemModalLabel">
        <div class="modal-dialog embeded-style " role="document">
            <div class="modal-content">
                <div class="modal-header-acoes">
                    <button type="button" class="close botao-fechar-consultar-fatura" id="fecharConsultarFatura"
                            data-dismiss="modal"
                            aria-label="Close"><span
                            aria-hidden="true">&times;</span></button>
                    <i class="material-icons money icone-acoes">monetization_on</i>
                    <h4 class="modal-title titulo-acoes">Consultar fatura</h4>
                </div>
                <div class="modal-body">
                    <div class="row coluna-acoes">
                        <div
                            class="col-sm-6 col-md-4 posicoes-coluna"
                            id="imprimirFaturaConsultarFatura"
                            v-if="consultar_fatura_modal.fn_carteira_cobranca_pix_gateway || consultar_fatura_modal.tipo_recebimento !== 'Pix' && consultar_fatura_modal.fn_carteira_cobranca_credit_card_gateway !== 'VD'"
                        >
                            <p class="descricao-acoes">Fatura</p>
                            <a class="cursor-pointer botoes_fechar" data-toggle="modal"
                               data-target="#modalImpressao"
                               v-tooltip.auto="{content: 'Imprimir Fatura', class: 'tooltip_padrao', delay: 1}"
                               v-on:click="chamarFuncaoHotsiteWeb('imprimirFatura',{'id_receber' : consultar_fatura_modal.id})"
                               v-if="consultar_fatura_modal.tipo_recebimento != 'Fatura'">
                                <i class="material-icons icones-coluna icone-fatura"
                                   v-bind:style=" 'color:'+consultar_fatura_modal.dados_logo_banco.cor_fundo_icone_banco"
                                >insert_drive_file</i>
                                <img v-bind:src="'assets/img/bancos/'+consultar_fatura_modal.dados_logo_banco.nome_arquivo_logo+'.svg'"
                                     class="logo-banco"
                                     v-if="consultar_fatura_modal.dados_logo_banco.nome_arquivo_logo != '' && !consultar_fatura_modal.dados_logo_banco.troca_cor_icone">
                                <img v-bind:src="'assets/img/bancos/'+consultar_fatura_modal.dados_logo_banco.nome_arquivo_logo+'.svg'"
                                     class="logo-banco logo-banco-branco"
                                     v-else-if="consultar_fatura_modal.dados_logo_banco.nome_arquivo_logo != '' && consultar_fatura_modal.dados_logo_banco.troca_cor_icone">
                            </a>
                            <a class="cursor-pointer" data-toggle="modal" data-target="#modalSelecionarBanco"
                               v-on:click="chamarFuncaoHotsiteWeb('montarModalSelecionarBanco',{'dados_receber' : consultar_fatura_modal})"
                               v-tooltip.top="{content: 'Selecionar banco da fatura', class: 'tooltip_padrao', delay: 1}"
                               v-else-if="carteiras_faturas[consultar_fatura_modal.id_carteira_cobranca].length > 1">
                                <span class="span-texto-imprimir">Imprimir</span>
                                <i class="material-icons  icones-coluna">insert_drive_file</i>
                            </a>
                            <a class="cursor-pointer botoes_fechar" data-toggle="modal"
                               data-target="#modalImpressao"
                               v-tooltip.auto="{content: 'Imprimir Fatura', class: 'tooltip_padrao', delay: 1}"
                               v-on:click="chamarFuncaoHotsiteWeb('imprimirFaturaComSelecaoDeBanco',{'dados_receber' : consultar_fatura_modal,'dados_carteira' : carteiras_faturas[consultar_fatura_modal.id_carteira_cobranca][0]})"
                               v-else-if="carteiras_faturas[consultar_fatura_modal.id_carteira_cobranca].length == 1">
                                <i class="material-icons icones-coluna icone-fatura"
                                   v-bind:style=" 'color:'+carteiras_faturas[consultar_fatura_modal.id_carteira_cobranca][0].dados_logo_banco.cor_fundo_icone_banco">insert_drive_file</i>
                                <img v-bind:src="'assets/img/bancos/'+carteiras_faturas[consultar_fatura_modal.id_carteira_cobranca][0].dados_logo_banco.nome_arquivo_logo+'.svg'"
                                     class="logo-banco"
                                     v-if="carteiras_faturas[consultar_fatura_modal.id_carteira_cobranca][0].dados_logo_banco.nome_arquivo_logo != '' &&  !carteiras_faturas[consultar_fatura_modal.id_carteira_cobranca][0].dados_logo_banco.troca_cor_icone">
                                <img v-bind:src="'assets/img/bancos/'+carteiras_faturas[consultar_fatura_modal.id_carteira_cobranca][0].dados_logo_banco.nome_arquivo_logo+'.svg'"
                                     class="logo-banco logo-banco-branco"
                                     v-else-if="carteiras_faturas[consultar_fatura_modal.id_carteira_cobranca][0].dados_logo_banco.nome_arquivo_logo != '' && carteiras_faturas[consultar_fatura_modal.id_carteira_cobranca][0].dados_logo_banco.troca_cor_icone">
                                <i class="material-icons icone-banco" v-else>account_balance</i>
                                <span class="span-texto-imprimir-content ">Imprimir</span>
                            </a>
                        </div>
                        <div class="col-sm-6 col-md-4 posicoes-coluna" id="imprimirPixConsultarFatura"
                             v-if="invoiceAllowsPaymentByPix(consultar_fatura_modal)">
                            <p class="descricao-acoes">Pix</p>
                            <a class="cursor-pointer botoes_fechar" id="botaoImprimirPixConsultarFatura"
                               data-toggle="modal"
                               data-target="#modalPix"
                               v-on:click="chamarFuncaoHotsiteWeb('imprimirPix', {'id_receber' : consultar_fatura_modal.id})"
                               v-tooltip.top="{content: 'Pix', class: 'tooltip_padrao', delay: 1}">
                                <i id="logoPixConsultarFatura" class="material-icons icones-coluna">pix</i>
                            </a>
                        </div>
                        <div class="col-sm-6 col-md-4 posicoes-coluna" id="creditCardConsultarFatura"
                             v-if="consultar_fatura_modal.habilitar_cartao == 'S'">
                            <p class="descricao-acoes">Cartão de crédito</p>
                            <router-link onclick="validaActiveClass('#pg_fatura')"
                                         :to="'/central_assinante_web/faturas/pagamentos/'+consultar_fatura_modal.id"
                                         class="cursor-pointer botoes_fechar" id="botaoPagarCreditCard"
                                         v-tooltip.top="{content: 'Pagar com cartão de cŕedito', class: 'tooltip_padrao', delay: 1}">
                                <i class="material-icons icones-coluna" id="iconeCreditCard">credit_card</i>
                            </router-link>
                        </div>
                        <div class="col-sm-6 col-md-4 posicoes-coluna" id="enviaEmailConsultarFatura"
                             v-if="email_fatura == 'S' && (consultar_fatura_modal.tipo_recebimento == 'Boleto' || consultar_fatura_modal.tipo_recebimento == 'Gateway')"
                             v-on:click="chamarFuncaoHotsiteWeb('enviarFaturaEMAIL',{'id_receber' : consultar_fatura_modal.id})"
                             v-tooltip.top="{content: 'Enviar fatura por email', class: 'tooltip_padrao', delay: 1}">
                            <p class="descricao-acoes">Email</p>
                            <a class="cursor-pointer botoes_fechar" id="botaoEnviaEmailConsultarFatura">
                                <i class="material-icons icones-coluna">mail</i>
                            </a>
                        </div>

                        <div class="col-sm-6 col-md-4 posicoes-coluna" id="enviaSmsConsultarFatura"
                             v-if="sms_fatura == 'S' && (consultar_fatura_modal.tipo_recebimento == 'Boleto' || consultar_fatura_modal.tipo_recebimento == 'Gateway')"
                             v-on:click="chamarFuncaoHotsiteWeb('enviarFaturaSMS',{'tipo' : 'sms', 'id_receber' : consultar_fatura_modal.id})"
                             v-tooltip.top="{content: 'Enviar fatura por SMS', class: 'tooltip_padrao', delay: 1}">
                            <p class="descricao-acoes">SMS</p>
                            <a class="cursor-pointer botoes_fechar" id="botaoEnviaSmsConsultarFatura">
                                <i class="material-icons icones-coluna">phone_iphone</i>
                            </a>
                        </div>
                        <div class="col-sm-6 col-md-4 posicoes-coluna" id="CopiarCodigoBarrasConsultarFatura"
                             v-if="(consultar_fatura_modal.tipo_recebimento == 'Boleto' || consultar_fatura_modal.tipo_recebimento == 'Gateway' || consultar_fatura_modal.tipo_recebimento == 'ArrecadacaoRecebimento')  && consultar_fatura_modal.linha_digitavel ">
                            <p class="descricao-acoes">Linha digitável</p>
                            <a class="cursor-pointer botoes_fechar" id="botaoCopiarCodigoBarrasConsultarFatura"
                               v-on:click="chamarFuncaoHotsiteWeb('copiarCodigoBarrasModal', {'linha_digitavel' : consultar_fatura_modal.linha_digitavel})"
                               v-tooltip.top="{content: 'Linha digitável ', class: 'tooltip_padrao', delay: 1}">
                                <i class="material-icons icones-coluna">content_copy</i>
                            </a>
                        </div>
                        <div class="col-sm-6 col-md-4 posicoes-coluna" id="imprimirGatewayAlternativaConsultarFatura"
                             v-if="consultar_fatura_modal.habilitar_gateway_alt == 'S'">
                            <p class="descricao-acoes">Imprimir Boleto</p>
                            <a class="cursor-pointer" id="botaoImprimirGatewayAlternativaConsultarFatura"
                               data-toggle="modal"
                               data-target="#modalImpressao"
                               v-on:click="chamarFuncaoHotsiteWeb('imprimirFatura',{'id_receber' : consultar_fatura_modal.id,'alt_gateway': true})"
                               v-tooltip.top="{content: 'Boleto', class: 'tooltip_padrao', delay: 1}">
                                <i id="logoImprimirGatewayAlternativa" class="material-icons icones-coluna">request_quote</i>
                            </a>
                        </div>
                    </div>
                </div>
                <div class="modal-overlay"></div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="modalSelecionarBanco" tabindex="-1" role="dialog"
         aria-labelledby="gridSystemModalLabel">
        <div class="modal-dialog embeded-style" role="document">
            <div class="modal-content">
                <div class="modal-header-acoes">
                    <button type="button" class="close botao-fechar-consultar-fatura" id="fecharModalSelecionarBanco"
                            data-dismiss="modal"
                            aria-label="Close"><span
                            aria-hidden="true">&times;</span></button>
                    <i class="material-icons money icone-acoes">monetization_on</i>
                    <h4 class="modal-title titulo-acoes">Imprimir fatura</h4>
                </div>
                <div class="modal-body">
                    <div class="row coluna-acoes">
                        <p class="texto-descricao">Clique em um dos bancos disponíveis para imprimir a fatura</p>
                        <div class="col-sm-6 col-md-4 col-xs-6 posicoes-coluna"
                             v-for="(carteira, index)  in carteiras_faturas[consultar_fatura_modal.id_carteira_cobranca] "
                             v-if="carteira.dados_logo_banco.nome_arquivo_logo != ''">
                            <a class="cursor-pointer"
                               v-tooltip.auto="{content: 'Imprimir Fatura', class: 'tooltip_padrao', delay: 1}"
                               data-toggle="modal"
                               data-target="#modalImpressao"
                               v-on:click="chamarFuncaoHotsiteWeb('imprimirFaturaComSelecaoDeBanco',{'dados_receber' : consultar_fatura_modal,'dados_carteira' : carteira})">
                                <img v-bind:src="'assets/img/bancos/'+carteira.dados_logo_banco.nome_arquivo_logo+'.svg'"
                                     class="logos-banco-coluna">
                            </a>
                            <p class="razao-banco"> {{carteira.dados_logo_banco.razao_banco}}</p>
                        </div>
                    </div>
                </div>
                <div class="modal-overlay"></div>
            </div>
        </div>
    </div>

    <div class="modal fade contato-modal" id="modalEntreEmContato" tabindex="-1" role="dialog"
         aria-labelledby="gridSystemModalLabel" v-if="cliente_nome">
        <div class="modal-dialog entre-em-contato-modal" role="document">
            <div class="modal-content">
                <div class="modal-header-entre-em-contato">
                    <button type="button" class="close botao-fechar-entre-em-contato" id="fecharModalEntreEmContato"
                            data-dismiss="modal"
                            aria-label="Close"><spanemail
                            aria-hidden="true">&times;</spanemail></button>
                    <i class="fas fa-envelope icone-acoes entre-em-contato-icone"></i>
                    <h4 class="modal-title titulo-acoes">Entre em contato</h4>
                </div>

                <div class="modal-body">
                    <div class="row coluna-acoes">
                        <div>
                            <a
                                    class="entre-em-contato-whatsapp"
                                    target="_blank" v-if="whatsapp_link" :href="whatsapp_link"
                                    v-tooltip.right="maximizado ? {content: 'Chame pelo WhatsApp', class: 'tooltip_padrao branco', delay: 1} : {class: 'tooltip_disabled'}"
                            >
                                <i class="fab fa-whatsapp"></i>
                                <p>Chamar no whatsapp</p>
                            </a>
                        </div>
                        <br>

                        <div>
                            <a
                                    class="entre-em-contato-chat"
                                    target="_blank" v-if="webchat_link" :href="webchat_link"
                                    v-tooltip.right="maximizado ? {content: 'Chame pelo chat', class: 'tooltip_padrao branco', delay: 1} : {class: 'tooltip_disabled'}"
                            >
                                <i class="fas fa-comment-alt"></i>
                                <p>Contato via chat</p>
                            </a>
                        </div>

                        <div class="telefone-contato container" v-if="telefone_contato">
                            <i class="fas fa-phone-alt"></i>
                            <p>{{telefone_contato}}</p>
                            <a class="cursor-pointer"
                               v-on:click="chamarFuncaoHotsiteWeb('copiarTelefoneContato', {'telefone_contato' : telefone_contato})"
                               v-tooltip.right="{content: 'Copiar número ', class: 'tooltip_padrao', delay: 1}">
                                <i class="material-icons" id="copiar_numero">content_copy</i>
                            </a>
                        </div>
                    </div>
                </div>

                <div class="modal-overlay"></div>
            </div>
        </div>
    </div>


    <div class="modal fade" id="modalAlterarDevice" tabindex="-1" role="dialog"
         aria-labelledby="gridSystemModalLabel" v-if="app.oner != 1">
        <div class="modal-dialog embeded-style " role="document">
            <div class="modal-content">
                <div class="modal-header-alteradevice">
                    <i class="fa fa-key icone-acoes"></i>
                    <h4 class="modal-title title-device-actions">Alterar dados do dispositivo</h4>
                    <button type="button" class="close botao-fechar-consultar-fatura" id="fecharAlterarConnections"
                            data-dismiss="modal"
                            aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>
                <div class="modal-body">
                    <div class="row coluna-acoes">
                        <div class="alterar-config-device-body">
                            <p>O que você deseja alterar?</p>
                            <div class="">
                                <select v-model="data_wifi.selectedWifi" class="form-control select-action-config form-select">
                                    <option disabled value="">Selecionar Opção...</option>
                                    <option v-for="interface in device_config_modal.interfaces" :key="`${interface.ssid}-${interface.id}`" :value="interface">
                                        {{ interface.ssid }} - {{ interface.tecnologia }}
                                    </option>
                                </select>
                                <br>
                            </div>

                            <div v-if="data_wifi.selectedWifi != '' ">
                                <input v-model="data_wifi.ssid" class="form-control input-actions" placeholder="Digite o novo nome da SSID">

                                <div class="input-actions">
                                    <input :type="inputType" v-model="data_wifi.passwordWifi1" class="form-control" placeholder="Digite a nova senha">
                                    <div class="icon-action button" v-on:click="inputType='text'" v-show="inputType=='password'">
                                        <i class="fa fa-eye"></i>
                                    </div>
                                    <div class="icon-action button" v-on:click="inputType='password'" v-show="inputType=='text'">
                                        <i class="fa fa-eye-slash"></i>
                                    </div>
                                </div>

                                <div class="span-actions">
                                    <span>DICA: Uma senha forte é composta obrigatoriamente por no mínimo 8 caracteres,
                                    sendo letras maiúsculas, minúsculas e números.
                                    Opcionalmente adicione caracteres especiais para deixa-la mais forte.</span>
                                </div>

                                <div  class="input-actions">
                                    <input :type="inputType2" v-model="data_wifi.passwordWifi2" class="form-control" placeholder="Digite a senha novamente">
                                    <div class="icon-action button" v-on:click="inputType2='text'" v-show="inputType2=='password'">
                                        <i class="fa fa-eye"></i>
                                    </div>
                                    <div class="icon-action button" v-on:click="inputType2='password'" v-show="inputType2=='text'">
                                        <i class="fa fa-eye-slash"></i>
                                    </div>
                                </div>

                                <div class="span-password-incorect"
                                     v-if="data_wifi.passwordWifi1 !== data_wifi.passwordWifi2 &&
                                     data_wifi.passwordWifi1 != '' &&
                                     data_wifi.passwordWifi2 != ''">
                                    <span>*As senhas são diferentes!</span>
                                </div>

                                <div class="span-password-incorect"
                                     v-if="this.data_wifi.passwordWifi1.length < 8 &&
                                     this.data_wifi.passwordWifi2.length < 8 &&
                                     this.data_wifi.passwordWifi1 != '' &&
                                     this.data_wifi.passwordWifi2 != ''">
                                    <span>*A senha deve conter mais de 8 caracteres!</span>
                                </div>
                            </div>

                            <div class="text-center">
                                <button class="btn button-devices"
                                        v-bind:class="classDisableButtonSalvar"
                                        v-on:click="chamarFuncaoHotsiteWeb('changedConfigWifi',{device_config_modal,data_wifi})">
                                    <i class="fa fa-floppy-disk-circle-arrow-right"></i>
                                    ATUALIZAR
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-overlay"></div>
            </div>
        </div>
    </div>

    <div id="preloader" v-bind:class="loading ? 'index-maximo' : 'index-minimo'">
        <div v-show="loading" id="loader"></div>
    </div>
</div>
</body>
</html>

<script type="text/javascript" src="/central_assinante_web/assets/lib/jquery_v3.2.1/js/jquery-3.2.1.min.js"></script>
<script type="text/javascript"
        src="/central_assinante_web/assets/lib/jquery_cookie_v1.4.1/js/jquery_cookie_v1.4.1.js"></script>
<script type="text/javascript" src="/central_assinante_web/assets/lib/bootstrap_v3.3.4/js/bootstrap.min.js"></script>
<script type="text/javascript" src="/central_assinante_web/assets/lib/vue_v2.5.11/js/vue.min.js"></script>
<script type="text/javascript" src="/central_assinante_web/assets/lib/vue_router_v3.0.1/js/vue-router.js"></script>
<script type='application/javascript' src='/central_assinante_web/assets/lib/fastclick/fastclick.min.js'></script>
<!-- Notificações -->
<script type="text/javascript" src="/central_assinante_web/assets/lib/iziToast_v1.3.0/js/iziToast.min.js"></script>
<!-- Tooltips -->
<script type="text/javascript"
        src="/central_assinante_web/assets/lib/VueDirectiveTooltip_v1.4.5/js/vueDirectiveTooltip.min.js"></script>
<!--Força da senha-->
<script type="text/javascript" src="/central_assinante_web/assets/lib/strength/js/strength.js"></script>
<!-- Signature -->
<script type="text/javascript"
        src="/central_assinante_web/assets/lib/signature_pad_v1.5.3/js/signature_pad.js"></script>
<!--CONFIGURAÇÕES-->
<script type="text/javascript" src="/central_assinante_web/assets/js/config.js"></script>
<!--CLASS WEB-->
<script type="text/javascript" src="/central_assinante_web/assets/js/classes/class_1.js"></script>
<script type="text/javascript" src="/central_assinante_web/assets/js/classes/SpeedTest.js"></script>
<!--ROTAS-->
<script type="text/javascript" src="/central_assinante_web/assets/js/rotas.js"></script>
<!-- LOGIN -->
<script type="text/javascript" src="/central_assinante_web/controller/login/login.js"></script>
<!-- CADASTRO LOGIN -->
<script type="text/javascript" src="/central_assinante_web/controller/cadastro_login/cadastro_login.js"></script>
<!--HOME-->
<script type="text/javascript" src="/central_assinante_web/controller/home/home.js"></script>
<!--DADOS CLIENTE-->
<script type="text/javascript" src="/central_assinante_web/controller/dados_cliente/dados_cliente.js"></script>
<!-- FATURAS -->
<script type="text/javascript" src="/central_assinante_web/controller/faturas/faturas.js"></script>
<!-- ATENDIMENTOS -->
<script type="text/javascript" src="/central_assinante_web/controller/atendimentos/atendimentos.js"></script>
<!-- CONTRATOS -->
<script type="text/javascript" src="/central_assinante_web/controller/planos/planos.js"></script>
<!-- NOTAS -->
<script type="text/javascript" src="/central_assinante_web/controller/notas/notas.js"></script>
<!-- CONSUMOS -->
<script type="text/javascript" src="/central_assinante_web/controller/consumos/consumos.js"></script>
<!-- CONECTIONS -->
<script type="text/javascript" src="/central_assinante_web/controller/connections/connections.js"></script>
<!-- CONFIGURAÇÕES PAGE -->
<script type="text/javascript" src="/central_assinante_web/controller/configuracoes/config.js"></script>
<!-- SPEED TEST -->
<script type="text/javascript" src="/central_assinante_web/controller/speed_test/speed_test.js"></script>
<!-- RELATORIOS -->
<script type="text/javascript" src="/central_assinante_web/controller/relatorios/relatorios.js"></script>
<!-- ERRO -->
<script type="text/javascript" src="/central_assinante_web/controller/erro/erro.js"></script>
<!--ChartJS-->
<script type="text/javascript" src="/central_assinante_web/assets/lib/Chartjs_v2.7.2/chart.min.js"></script>
<!--HighCharts-->
<script type="text/javascript" src="/central_assinante_web/assets/lib/highcharts_v9.0.0/highcharts.js"></script>
<script type="text/javascript" src="/central_assinante_web/assets/lib/highcharts_v9.0.0/highcharts-more.js"></script>
<!-- Card Type-->
<script type="text/javascript" src="/central_assinante_web/assets/lib/Cardtype_v0.0.2/card-type.js"></script>
<!-- Mobile Events-->
<script src="/central_assinante_web/assets/lib/jqueryTouchEvents_v1.0.5/jquery.mobile-events.js"></script>
<!--Cartão de crédito-->
<script type="text/javascript" src="/central_assinante_web/assets/lib/card-master/js/card.js"></script>
<script type="text/javascript" src="/central_assinante_web/assets/js/classes/CreditCardException/CreditCardsExceptions.js"></script>
<!-- vueMask-->
<script src="/central_assinante_web/assets/lib/VueTheMask_v0.11.1/vue-the-mask.min.js"></script>
<!-- Main -->
<script type="text/javascript" src="/central_assinante_web/assets/js/main.js"></script>
<!-- Captcha -->
<script type="text/javascript" src="/central_assinante_web/assets/lib/alphanumeric-captcha/js/jquery-captcha.min.js"></script>
<!-- MD5 -->
<script type="text/javascript"
        src="/central_assinante_web/assets/lib/md5-js/md5.min.js"></script>
