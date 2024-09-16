# Trabalho 1 - SISOP - Leonardo Silveira da Cruz

Link para o projeto: https://github.com/leodacruz/Sisop_Trab_1

# Como executar 1
abaixo a forma de como executar, da forma normal. Deste jeito ele executa com base nos processos que estão no arquivo processos.json, que está na pasta
resources, caminho:  app -> src -> main -> resources

## windows
### gradlew run
Basta fazer o comando: gradlew run

## linux
### ./gradlew run
caso ele reclame de permissão faça o comando:  chmod +x gradlew 
que depois ele deixa rodar o comando normal

# Importante
Caso tente executar usando outra forma que não seja o gradle (dando f5 pelo vscode, apertando o play e assim vai) lembre de descomentar a linha 55 que está 
assim: // + File.separator + "app" 

se não descomentar ele vai acusar um erro, então caso não use o gradle lembre de descomentar essa linha para o programa funcionar


# Como executar 2
Está forma de executar é para quando quer executar n processos, esses gerados de forma aleatoria. Atualmente não existe limites para quantos processos, mas sim 
um limite nos parametros que são os seguintes: 
### surtoCPU =    Valor aleatório entre 0 e 9  -> caso seja 0, o valor de tempoES tb é 0
### tempoES =     Valor aleatório entre 1 e 10
### tempoTotal =  Valor aleatório entre 1 e 20
### prioridade  = Valor aleatório entre 0 e 10


## windows
#### gradlew run --args='x'     
sendo o x um numero, exemplo: gradlew run --args='10'

## linux
#### ./gradlew run --args='x'     
sendo o x um numero, exemplo: ./gradlew run --args='10'


# Imagem
existe uma imagem que mostra um mapa de como os metodos funcionam, pode ajudar a entender a logica absurda atual