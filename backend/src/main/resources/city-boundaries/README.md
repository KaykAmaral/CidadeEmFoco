# Limites municipais

Os arquivos GeoJSON desta pasta são usados pelo backend para validar se uma
ocorrência foi registrada dentro da cidade vinculada ao cidadão.

Cada arquivo deve ser nomeado com o código IBGE de sete dígitos da cidade, por
exemplo `3541000.geojson` para Praia Grande (SP). Antes de habilitar uma nova
cidade no sistema, adicione aqui seu limite municipal.

O limite de Praia Grande foi obtido pela API de Malhas do IBGE, usando a
qualidade mínima para manter o arquivo pequeno:

`https://servicodados.ibge.gov.br/api/v3/malhas/municipios/3541000?formato=application/vnd.geo%2Bjson&qualidade=minima`
