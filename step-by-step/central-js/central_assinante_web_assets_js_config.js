//CONSTANTES DO SISTEMA
const protocolo = location.protocol;
const host = window.location.hostname;
const port = window.location.port;
const __SERVER__ = port ? (protocolo + '//' + host + ':' + port + '/central_assinante_web') : (protocolo + '//' + host + '/central_assinante_web');
const __ROTAS__ = [];
const __DADOS_CLIENTE__ = [];
const __PARAMETROS__ = [];

//VARIAVEIS GLOBAIS LET
let PAGE_HOME = false;
let PAGE_DADOS_CLIENTE = false;
let PAGE_ATENDIMENTOS = false;
let PAGE_MENSAGENS_ATENDIMENTO = false;
let PAGE_NOVO_ATENDIMENTO = false;
let PAGE_FATURAS = false;
let PAGE_PAGAMENTOS = false;
let PAGE_PLANOS = false;
let PAGE_NOTAS = false;
let PAGE_ERRO = false;
let PAGE_CONSUMOS = false;
let PAGE_RELATORIOS = false;
let PAGE_CONFIG = false;
let PAGE_SPEEDTEST = false;
let PAGE_CADASTRO_LOGIN = false;
let PAGE_CONNECTIONS = false;
let PAGE_RECORRENCIA_VINDI = false;
let CAD_LOGIN = true;

//VARIAVEIS GLOBAIS PARA TRATAR LOADING
LOADING_FATURA = false;
LOADING_FRANQUIA = false;
LOADING_PLANO = false;
LOADING_ATENDIMENTO = false;
LOADING_DADOS = false;
LOADING_LOGIN = false;
LOADING_NOTAS = false;
LOADING_DEBITO = false;
LOADING_CONSUMO = false;
LOADING_CONFIG = false;
LOADING_CONNECTIONS = false;
CLIENTE = "";
BLOB = null;
FILENAME = null;

//VARIAVEIS GLOBAIS PARA CARREGAR MAIS DADOS
SLICE_FATURA = 5;
SLICE_PLANO = 5;
SLICE_NOTA = 5;
SLICE_CONSUMO = 5;
SLICE_ATENDIMENTO = 5;
FATURA_TOAST = true;
PLANO_TOAST = true;
ATENDIMENTO_TOAST = true;
ANIMA_ICONE = false;
IS_SAFARI = false;
IS_REMOVED = false;
INDEX_AUTOCOMPLETE = 0;
