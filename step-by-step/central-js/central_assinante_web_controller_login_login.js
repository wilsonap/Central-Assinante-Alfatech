$.get('/api-module/central-api/get-model-central', (response) => {

    if(response.data.modelCentral === 'N') {
        const host = window.location.hostname;
        const protocolo = location.protocol;
        window.location.href = protocolo + '//' + host + '/central-assinante';
    }
})
login();
trocarSenha();

var funcoes_login = {
    validarLogin(user, password) {
        router.history.current.matched[0].instances.default.loading = true;
        var manter_conectado = router.history.current.matched[0].instances.default.manter_conectado;

        var hs_web = new HotsiteWeb();
        hs_web.iniciarSessao(user, password);
        if (manter_conectado === true) {
            $.cookie('manter_conectado', 'S');
        } else {
            $.cookie('manter_conectado', 'N');
        }
    },
    recuperarSenha(user) {
        var hs_web = new HotsiteWeb();
        router.history.current.matched[0].instances.default.loading_senha = true;

        if (user === '') {
            hs_web.criarToast('5000', 'Erro!', 'Informe o Login!', 'fas fa-exclamation-circle', 'red', 'id_erro', true, '', 'topCenter');
        } else {
            hs_web.validaCamposRecuperaSenha(user).then((retorno)=>{
                if (retorno){
                    router.history.current.matched[0].instances.default.habilitar_form_primeiro_acesso = true;
                    router.history.current.matched[0].instances.default.user = user;
                    hs_web.getRecuperaSenhaTipo().then((tipo)=>{
                        router.history.current.matched[0].instances.default.recupera_senha_tipo=tipo;
                        router.history.current.matched[0].instances.default.esqueceu_senha = true;
                    })
                }else{
                    hs_web.criarToast('5000', 'Erro!', 'Login inválido!', 'fas fa-exclamation-circle', 'red', 'id_erro', true, '', 'topCenter');
                }
            })

        }
        router.history.current.matched[0].instances.default.loading_senha = false;
    },
    timer(time){
        return '0'+parseInt(time/60)+':'+(time%60<10?'0':'')+time%60
    },
    setTimer(){
        counter = setInterval(()=>{
            if(router.history.current.matched[0].instances.default.cooldown>0){
                router.history.current.matched[0].instances.default.cooldown-=1
            }else{
                router.history.current.matched[0].instances.default.cooldown=300;
                clearInterval(counter)
            }
        }, 1000);
    },
    voltarParaLogin() {
        window.location.reload();
    },
    cadastrar(nome_pre_cadastro, telefone_pre_cadastro, cpf_cnpj_pre_cadastro, email_pre_cadastro, senha_pre_cadastro, confirmar_senha_pre_cadastro, tipo_login_cadastro, captcha_cad) {
        var hs_web = new HotsiteWeb();

        array_dados = {
            nome: nome_pre_cadastro,
            telefone: telefone_pre_cadastro,
            cpf_cnpj: cpf_cnpj_pre_cadastro,
            email: email_pre_cadastro,
            senha: senha_pre_cadastro,
            confirmar_senha: confirmar_senha_pre_cadastro,
            tipo_login_cadastro: tipo_login_cadastro,
            captcha: captcha_cad,
            captcha_esperado: window.captcha
        };

        if(array_dados.nome == ""){

            hs_web.criarToast('5000', 'Erro!', 'Nome é obrigatório', 'fas fa-exclamation-circle', 'red', 'id_erro');
            return 0;
        }

        array_dados.telefone = array_dados.telefone .replace(/[\(\)\.\s-]+/g,'');

        if(array_dados.telefone == ""){
            hs_web.criarToast('5000', 'Erro!', 'Telefone é obrigatório', 'fas fa-exclamation-circle', 'red', 'id_erro');
            return 0;
        }

        if (array_dados.telefone.length < 10){
            hs_web.criarToast('5000', 'Erro!', 'Telefone inválido', 'fas fa-exclamation-circle', 'red', 'id_erro');
            return 0;
        }

        array_dados.cpf_cnpj = array_dados.cpf_cnpj.replace(/[\(\)\.\s-/]+/g,'');

        if(array_dados.cpf_cnpj == ""){
            hs_web.criarToast('5000', 'Erro!', 'CPF/CNPJ é obrigatório', 'fas fa-exclamation-circle', 'red', 'id_erro');
            return 0;
        }

        if (array_dados.cpf_cnpj.length != 11 && array_dados.cpf_cnpj.length != 14){
            hs_web.criarToast('5000', 'Erro!', 'CPF/CNPJ inválido', 'fas fa-exclamation-circle', 'red', 'id_erro');
            return 0;
        }

        if(array_dados.email == ""){
            hs_web.criarToast('5000', 'Erro!', 'E-mail é obrigatório', 'fas fa-exclamation-circle', 'red', 'id_erro');
            return 0;
        }

        if(!hs_web.validarEmail(array_dados.email)){
            hs_web.criarToast('5000', 'Erro!', 'E-mail inválido', 'fas fa-exclamation-circle', 'red', 'id_erro');
            return 0;
        }

        if (array_dados.captcha == ""){
            hs_web.criarToast('5000', 'Erro!', 'Preencha o captcha!', 'fas fa-exclamation-circle', 'red', 'id_erro');
            return 0;
        }

        let captchaRequestData = {
            text: captcha_cad,
            cacheKey: document.getElementById('captchaKey').value
        }
        $.get('/api-module/auth/validate-captcha', captchaRequestData, (response) => {
            if (response.status === 'error') {
                hs_web.refreshCaptchaImage('captchaRegisterImage')
                hs_web.criarToast('5000', 'Erro!', 'Captchas diferentes!', 'fas fa-exclamation-circle', 'red', 'id_erro');
                document.getElementById('input_captcha').value = '';
                app.loading = false;
                return 0;
            } else if (response.status === 'success') {
                hs_web.cadastrarNovoCliente(array_dados);
            }
        }).fail((response) => {
            if (response.status === 498) {
                hs_web.refreshCaptchaImage('captchaRegisterImage')
                document.getElementById('input_captcha').value = '';
                hs_web.criarToast('3000', 'Erro!', 'O tempo para o CAPTCHA expirou. Realize a operação novamente.', 'fas fa-exclamation-circle', 'yellow', 'id_erro')
                app.loading = false
                return 0;
            }
        })
    },

    resetarSenhaAoLogarPelaPrimeiraVez(cpf_cnpj, tel_email, user){
        var hotsiteWeb = new HotsiteWeb();

        if (!cpf_cnpj){
            hotsiteWeb.criarToast('6000', 'Erro!', 'CPF/CNPJ não informado', 'fas fa-exclamation-circle', 'red', 'id_erro', true, '', 'topCenter');
            hotsiteWeb.resetaCooldown();
            return 0;
        }

        let instdefault = router.history.current.matched[0].instances.default;

        hotsiteWeb.validaCamposRecuperaSenha(instdefault.user,cpf_cnpj,tel_email).then((retorno)=>{
            if(retorno){
                if (instdefault.recupera_senha_tipo == 'S'){
                    hotsiteWeb.recuperarSenha(tel_email, cpf_cnpj)
                }else{
                    hotsiteWeb.enviarEmailParaResetarSenha({ cpfCnpj:cpf_cnpj, email:tel_email})
                }
            }else{
                hotsiteWeb.criarToast('6000', 'Erro!', 'Os dados informados são inválidos', 'fas fa-exclamation-circle', 'red', 'id_erro', true, '', 'topCenter');
                hotsiteWeb.resetaCooldown()
            }
        })
    },
};

function login() {
    try {
        $.ajax({
            url: __SERVER__ + "/view/login/login.vue",
            type: "GET",
            data: "",
            dataType: 'html'
        }).done(function (resposta) {
            __ROTAS__.push({
                path: '/central_assinante_web/login',
                name: 'login',
                component: {
                    template: resposta,
                    data() {
                        return {
                            user: '',
                            password: '',
                            url_video: '',
                            tipo_login: 'E',
                            permitir_cadastros_usuario: 'N',
                            logo_base: '',
                            logo_base_login: '',
                            label_login:'',
                            mask: false,
                            loading: false,
                            loading_senha: false,
                            manter_conectado: false,
                            nome_pre_cadastro: '',
                            telefone_pre_cadastro: '',
                            cpf_cnpj_pre_cadastro: '',
                            email_pre_cadastro: '',
                            senha_pre_cadastro: '',
                            confirmar_senha_pre_cadastro: '',
                            captcha_pre_cadastro: '',
                            habilitar_form_primeiro_acesso: false,
                            cpf_cnpj_reset_password: '',
                            email_reset_password: '',
                            esqueceu_senha: false,
                            cooldown:300,
                            recupera_senha_tipo:'',
                            email:'',
                            cpf_cnpj:'',
                            telefone_celular:''
                        }
                    },
                    mounted: function () {
                        if ($(window).width() >= 768) {
                            $('.login-mobile').remove();

                            var videos = [
                                {nome: 'back-login-1.mp4'},
                                {nome: 'back-login-2.mp4'},
                                {nome: 'back-login-3.mp4'}
                            ];

                            var num_rand = Math.floor((Math.random() * videos.length) + 1);
                            var url_video = '/central_assinante_web/assets/movie/' + videos[num_rand - 1].nome;
                            router.history.current.matched[0].instances.default.url_video = url_video;
                        } else {
                            $('.login-principal').remove();
                        }

                        $(document).ready(function () {
                            validarFundoLogin();
                        });
                    },
                    methods: {
                        ...funcoes_login,
                        aplicarMascaraCpfCnpj(event, campo) {
                            let valor = event.target.value;
                            valor = valor.replace(/[^a-zA-Z0-9]/g, '').toUpperCase();
                            const temLetra = /[a-zA-Z]/.test(valor.substring(0, 12));
                            let valorFormatado = '';
                            
                            if (temLetra) {
                                let parteAlfa = valor.substring(0, 12);
                                let parteDV = valor.substring(12).replace(/[^0-9]/g, '').substring(0, 2);
                                valor = parteAlfa + parteDV;
                                
                                for (let i = 0; i < valor.length; i++) {
                                    if (i === 2 || i === 5) valorFormatado += '.';
                                    if (i === 8) valorFormatado += '/';
                                    if (i === 12) valorFormatado += '-';
                                    valorFormatado += valor[i];
                                }
                            } else {
                                if (valor.length <= 11) {
                                    for (let i = 0; i < valor.length; i++) {
                                        if (i === 3 || i === 6) valorFormatado += '.';
                                        if (i === 9) valorFormatado += '-';
                                        valorFormatado += valor[i];
                                    }
                                } else {
                                    valor = valor.substring(0, 14);
                                    for (let i = 0; i < valor.length; i++) {
                                        if (i === 2 || i === 5) valorFormatado += '.';
                                        if (i === 8) valorFormatado += '/';
                                        if (i === 12) valorFormatado += '-';
                                        valorFormatado += valor[i];
                                    }
                                }
                            }
                            
                            this[campo] = valorFormatado;
                            event.target.value = valorFormatado;
                        }
                    }
                }
            });
        })
    } catch (e) {
        console.log("Error message ->" + e);
    }
}

var funcoes_troca_senha = {
    salvarNovaSenha(password, confirm){
        var hotsiteWeb = new HotsiteWeb();
        if (!password){
            hotsiteWeb.criarToast('5000', 'Erro!', 'Senha é obrigatório', 'fas fa-exclamation-circle', 'red', 'id_erro');
        }else if(password.length < 8){
            hotsiteWeb.criarToast('5000', 'Erro!', 'A senha deve conter pelo menos 8 caracteres', 'fas fa-exclamation-circle', 'red', 'id_erro');
        }else if(!confirm){
            hotsiteWeb.criarToast('5000', 'Erro!', 'Confirmar senha é obrigatório', 'fas fa-exclamation-circle', 'red', 'id_erro');
        }else if(password !== confirm){
            hotsiteWeb.criarToast('5000', 'Erro!', 'As senhas não conferem', 'fas fa-exclamation-circle', 'red', 'id_erro');
        }else{
            let hashPassword = localStorage.getItem('trocarSenhaHash');
            console.log(hashPassword)
            if (!hashPassword){
                hotsiteWeb.criarToast('5000', 'Erro!', 'Link inválido', 'fas fa-exclamation-circle', 'red', 'id_erro');
            }else{
                hashPassword = JSON.parse(hashPassword);
                hotsiteWeb.salvarNovaSenha({ password, hashPassword });
            }
        }
    }
}

function trocarSenha() {
    try {
        $.ajax({
            url: __SERVER__ + "/view/login/trocar_senha.vue",
            type: "GET",
            data: "",
            dataType: 'html'
        }).done(function (resposta) {
            __ROTAS__.push({
                path: '/central_assinante_web/trocarSenha',
                name: 'trocar_senha',
                component: {
                    template: resposta,
                    data() {
                        return {
                            hash_senha: '',
                            new_password: '',
                            confirm_password: '',
                        }
                    },
                    methods: funcoes_troca_senha
                }
            });
        })
    } catch (e) {
        console.log("Error message ->" + e);
    }
}

var videos = [
    {nome: 'back-login-1.mp4'},
    {nome: 'back-login-2.mp4'},
    {nome: 'back-login-3.mp4'}
];

var num_rand = Math.floor((Math.random() * videos.length) + 1);
var url_video = '/central_assinante_web/assets/movie/' + videos[num_rand - 1].nome;

$(document).ready(function () {
    if (router.history.current.name === 'login') {
        var hs_web = new HotsiteWeb();
        var refreshCaptcha = hs_web.refreshCaptchaImage

        let captchaRequestData = {
            length: 7,
            height: 40,
            width: 200
        };
        let xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function() {
            if (this.readyState === 4 && this.status === 200) {
                let img = document.createElement('img')
                let response = JSON.parse(xhttp.responseText);
                img.id = 'captchaRegisterImage'
                img.src = 'data:image/png;base64,' + response.data.captchaImageBase64;
                img.style.width = captchaRequestData.width
                img.style.height = captchaRequestData.height
                img.addEventListener('click', (event) => {
                    refreshCaptcha(event.srcElement.id)
                })
                document.getElementById('rowCaptcha').prepend(img)
                document.getElementById('captchaKey').value = response.data.cacheKey
            }
        };
        xhttp.open("GET", '/api-module/auth/generate-captcha-image?length=7&height=40&width=200', true);
        xhttp.send();
    }
});
