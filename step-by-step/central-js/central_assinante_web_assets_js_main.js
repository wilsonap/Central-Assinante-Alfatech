isSafari();

$sidebar = $('.sidebar');
window_width = $(window).width();

/**
 * Abrir menu lateral
 */
$(document).on('click', '.navbar-toggle', function () {
    $toggle = $(this);

    div = '<div id="bodyClick"></div>';
    $(div).appendTo('body').click(function () {
        $('html').removeClass('nav-open');
        mobile_menu_visible = 0;
        setTimeout(function () {
            $toggle.removeClass('toggled');
            $('#bodyClick').remove();
        }, 550);
    });

    $('html').addClass('nav-open');
});

function validarFundoLogin() {
    let container_login = $('#app:has(.app-login)');

    if (container_login.length === 1) {
        $('#app').css("background-image", "linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)");
    } else {
        $('#app').css("background", "#e2e2e2");
    }
}

/**
 * Instancia força da senha
 */
function forcaSenha() {
    var senha = $('#senha');

    if (senha) {
        senha.strength({
            strengthClass: 'strength',
            strengthMeterClass: 'strength_meter',
            strengthButtonClass: 'button_strength'
        });
    }
}

function validaActiveClass(el) {
    $(document).ready(function () {
        $('.menu-item').removeClass('active');
        $(el).addClass('active');
        $('html').removeClass('nav-open');
        $('.navbar-toggle').removeClass('toggled');
        $('#bodyClick').remove();
    });
}

function ativaSwipe() {
    $(document).ready(function () {
        $('.main-panel').on('swipeleft', function (e) {
            new HotsiteWeb().getDadosServidor();
            if (app.assinando === false) {
                div = '<div id="bodyClick"></div>';
                $('html').addClass('nav-open');
                mobile_menu_visible = 0;

                $(div).appendTo('body').click(function () {
                    $('.navbar-toggle').removeClass('toggled');
                    $('html').removeClass('nav-open');
                    $('#bodyClick').remove();
                    mobile_menu_visible = 1;
                });
            }
        });

        $('body').on('swiperight', function (e) {
            if (app.assinando === false) {
                $('.navbar-toggle').removeClass('toggled');
                $('html').removeClass('nav-open');
                $('#bodyClick').remove();
                mobile_menu_visible = 1;
            }
        });
    });
}

function swipeLeftSidebar() {
    $(document).ready(function () {
        $('.sidebar-wrapper').on('swiperight', function (e) {
            $('.sidebar').removeClass('minimizado').addClass('maximizado');
            $('.sidebar-wrapper').removeClass('minimizado').addClass('maximizado');
            $('.main-panel').removeClass('maximizado').addClass('minimizado');
            $('button.maximizar').removeClass('maximizar').addClass('minimizar');
            $('.gesture-icon').text('chevron_left');
            maximizado = false;
        });

        $('.sidebar-wrapper').on('swipeleft', function (e) {
            $('.sidebar').removeClass('maximizado').addClass('minimizado');
            $('.sidebar-wrapper').removeClass('maximizado').addClass('minimizado');
            $('.main-panel').removeClass('minimizado').addClass('maximizado');
            $('button.maximizar').removeClass('minimizar').addClass('maximizar');
            $('.gesture-icon').text('chevron_right');
            maximizado = true;
        });
    });
}

function iniciaFirebase() {
    $(document).ready(function () {
        if (typeof window.ReactNativeWebView != 'undefined') {
            const hs_web = new HotsiteWeb();
            hs_web.updateFirebaseToken();
        } else if (app.cordova_app){
            window.FirebasePlugin.getToken(function(token) {
                // save this server-side and use it to push notifications to this device
                console.log(token);
            }, function(error) {
                console.error(error);
            });
        }

    });
}

$(window).resize(function () {
    var hs_web = new HotsiteWeb();
    if (ROTA == "/central_assinante_web/") {
        hs_web.validaResizeHome();
    } else if (ROTA == "/central_assinante_web/atendimentos") {
        hs_web.validaResizeAtendimentos();
    } else if (ROTA == "/central_assinante_web/faturas") {
        hs_web.validaResizeFaturas();
    } else if (ROTA == "/central_assinante_web/planos") {
        hs_web.validaResizePlanos();
    } else if (ROTA == "/central_assinante_web/notas") {
        hs_web.validaResizeNotas();
    }
});

function isSafari() {
    var ios = navigator.userAgent.match(/ipad|ipod|iphone/i);
    var safari = navigator.userAgent.match(/^((?!chrome|android).)*safari/i);

    if (ios || safari) {
        IS_SAFARI = true;
    } else {
        IS_SAFARI = false;
    }
}

function setarPermissaoHome() {
    router.history.current.matched[0].instances.default.mostrar_franquia = app.pg_consumo;
    router.history.current.matched[0].instances.default.mostrar_fatura = app.pg_fatura;
    router.history.current.matched[0].instances.default.mostrar_plano = app.pg_plano;
    router.history.current.matched[0].instances.default.mostrar_atendimento = app.pg_atendimento;
    router.history.current.matched[0].instances.default.sms_fatura = app.sms_fatura;
    router.history.current.matched[0].instances.default.email_fatura = app.email_fatura;
    router.history.current.matched[0].instances.default.imprime_venda_fatura = app.imprime_venda_fatura;
}

function buttonFileClick(id_button) {
    if (id_button) {
        $(id_button).click();
    }
}

function isFirefox() {
    var isFirefox = typeof InstallTrigger !== 'undefined';

    if (isFirefox && !IS_REMOVED) {
        document.getElementById('animate_link').remove()
        IS_REMOVED = true;
    }
}

(function ($, window, document) {

    var $html = $('html');

    $html.on('click.ui.dropdown', '.js-dropdown', function (e) {
        $('.js-dropdown ul').show();
        $('.js-dropdown ul').not($(this).find('ul')).hide();
    });

    $html.on('click.ui.dropdown', function (e) {
        var $target = $(e.target);
        if (!$target.parents().hasClass('js-dropdown')) {
            $('.js-dropdown ul').hide();
        }
    });

})(jQuery, window, document);

$(function () {
    if(typeof Origami !== 'undefined') {
        var attachFastClick = Origami.fastclick;
        attachFastClick(document.body);
    }
});

$(document).click(function (document) {
    $(document).ready(function () {
        if (!$(document.target).hasClass('options') && !$(document.target).hasClass('cidades') && (ROTA == "/central_assinante_web/dados_cliente" || ROTA == "/central_assinante_web/dados_cliente/")) {
            router.history.current.matched[0].instances.default.mostra_autocomplete = false;
        }else if($(document.target).hasClass('options') && $(document.target).hasClass('cidades') && (ROTA == "/central_assinante_web/dados_cliente" || ROTA == "/central_assinante_web/dados_cliente/")){
            router.history.current.matched[0].instances.default.mostra_autocomplete = true;
        }
    })
});

function openLink(link){
    window.open(link['link'], '_system', 'location=no');
}

function generateApp(el) {
    validaActiveClass(el);
    openBuilder();
}
function openBuilder() {
    try {
        const item = $.cookie('manter_conectado') === 'S' ? localStorage.getItem('dados') : sessionStorage.getItem('dados');
        const parsedData = JSON.parse(item);
        const { id, fantasia, razao } = parsedData;

        const form = document.createElement('form');
        form.setAttribute('method', 'post');
        form.setAttribute('action', app.url_generate_app);
        form.setAttribute('target', '_blank');

        const idInput = document.createElement('input');
        idInput.setAttribute('name', 'ID');
        idInput.setAttribute('value', id);

        const fantasiaInput = document.createElement('input');
        fantasiaInput.setAttribute('name', 'FANTASIA');
        fantasiaInput.setAttribute('value', fantasia);

        const razaoInput = document.createElement('input');
        razaoInput.setAttribute('name', 'RAZAO');
        razaoInput.setAttribute('value', razao);

        form.appendChild(idInput);
        form.appendChild(fantasiaInput);
        form.appendChild(razaoInput);

        document.body.appendChild(form);
        form.submit();
        document.body.removeChild(form);
    } catch (e) {
        console.log(e);
    }
}
