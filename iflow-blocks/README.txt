Compilar e mover as classes do projecto para ${iflow.home}/repository_data/1/Classes


Adicionar Novos campos ao Formulario.
    1 - JSP<Denomina��o do novo Campo>Data
        - Localiza��o: "pt.iknow.floweditor.blocks"
        - Propriedades que ser�o apresentadas no editor, usadas para configura��o do campo

    2 - pt.iflow.blocks.form.<Denomina��o do novo Campo>
        - Gera��o de xml e valores importantes a serem usados quando o fluxo esta a gerar o formulario

    3 - Classe JSPFieldTypeEnum
        - Adicionar entrada na classe JSPFieldTypeEnum, campos por ordem:
            - code -> inteiro que tamb�m permitar� a ordena��o dos campos na combobox do dialogo adicionar campo
            - descrKey -> Nome do campo definida no ficheiro "editor_blocks.properties" (Apresentada na combobox do novo campo)
            - tooltipKey -> tooltip do campo definida no ficheiro "editor_blocks.properties" (Apresentada na combobox do novo campo)
            - editorClass -> Nome da classe com as defini��es das propriedades do novo campo (criada no ponto 1)
            - engineClass -> classe com caminho que � responsavel pela constru��o do campo (xml) (criada no ponto 2)
            - extraButtons -> Bot�es adicionar e remover linha (nas propriedades multiplas)

    4 - JSPFieldData.java
        - Localiza��o dos "tipos" de campos, que podem ser usados nas propriedades do ponto 1
        - Defini��o de combobox � realizada aqui.

        - _hsDisableDataTypes - listagem de tipos de dados que nao podem ser usados na variavel
