Instalar nova inst�ncia/servi�o do Tomcat:

Criar a seguinte estrutura de direct�rios, copiados a partir do 
direct�rio de instala��o do Tomcat:
    - conf
    - logs
    - temp
    - webapps
    - work

No direct�rio "conf", alterar os portos configurados no ficheiro 
"server.xml" de forma a que n�o entrem em conflito com aplica��es
existentes. Os portos a alterar s�o:
 - <Server port="">;
 - <Connector port="">; (HTTP e AJP)

Modificar o ficheiro "service.bat" de forma a corrigir os PATHs para
as aplica��es/instala��es/etc. Consultar:
http://tomcat.apache.org/tomcat-6.0-doc/printer/windows-service-howto.html

executar o ficheiro "service.bat" para criar o servi�o "Apache Tomcat iFlow":
> service.bat install iFlow

Para remover o servi�o:
> service.bat remove iFlow

