# ![Logo-template](images/logo-template.png)
# Recurso APX Online insuranceroyal-trx5

> El objetivo de este documento es proveer información relacionada del API "vehicle-insurances" que utiliza este proyecto y que ha sido implementado en APX y desplegado en la consola de la plataforma.


### 1. API

> Datos del API de Catalogo implementado en el runtime de APX Online.

- API a implementar: [vehicle-insurances](https://catalogs.platform.bbva.com/apicatalog/business/apis/apis-insurances-vehicleinsurances/versions/pe-0.9.0/)
- SN del servicio: *(SNPE1800110)*

### 2. Servicio:

> En este apartado se detallan los endpoints implementados del API vehicle-insurances.

- listRiskBlackListedEntities
    - SMC del Servicio: [Documento](https://docs.google.com/spreadsheets/d/14wQl0dfnhHZ4gMBeC193k1qRINVfTLkmXe1INkq1GHA/edit#gid=1224121798)
    - Método HTTP: GET
    - Versión: 1.34.0
    - Endpoint: /risks/v1/black-listed-entities
    - TX: [PISDT005](#PISDT005)

### 3. DTOs:

> En este apartado se detallan todas las clases DTOs utilizadas en este recurso.

- **PISDC011**:
  - amazon:
      - **SignatureAWS**: Entidad SignatureAWS
  - aso:
      - **BlackListASO**: Entidad BlackList
  - blacklist:
      - **BlackListTypeDTO**: Entidad tipo de lista negra
      - **BlockingCompanyDTO**: Entidad empresa de bloqueo
      - **EntityOutBlackListDTO**: Entidad de salida
      - **InsuranceBlackListDTO**: Entidad lista negra
      - **BlockTypeDTO**: Entidad tipo de bloqueo
      - **BlackListRequestRimacDTO**: Entidad solicitud lista negra
  - commons:
      - **IdentityDocumentDTO**: Entidad documento de identidad
      - **IdentityDataDTO**: Entidad dato de identidad
      - **DocumentTypeDTO**: Entidad tipo de documento
      - **InsuranceProductDTO**: Entidad producto
  - bo:
      - **BlackListIndicatorBO**: Entidad BlackListIndicator
      - **SelectionQuotationPayloadBO**: Entidad SelectionQuotationPayload
      - **BlackListHealthRimacBO**: Entidad BlackListHealthRimac
      - **BlackListRiskRimacBO**: Entidad BlackListRiskRimac
  - utils:
      - **PISDConstants**: Entidad Constantes
      - **PISDErrors**: Entidad Errores
      - **PISDProperties**: Entidad Propiedades


### 4. Transacciones APX:
> En este apartado se detallan todas las transacciones creadas para soportar las operaciones del servicio implementado.
- Usuario transaccional: ZG13003 y ZG13001
- **TX - PISDT005**: Obtiene una validación de lista negra para el servicio listRiskBlackListedEntities de vehicle-insurances
    - Código de respuesta: Http Code: 200, Severity: OK
    - Código de Respuesta: Http Code: 400, Severity: ENR

### 5. Librerías internas:
> En este apartado se detallan las librerías internas creadas para implementar la lógica de negocio del servicio.

- **PISDR008**: [Ver documentación](PISD_APX_Library_PISDR008.md)
- **PISDR018**: [Ver documentación](PISD_APX_Library_PISDR018.md)

### 6. Librerías externas:
> En este apartado se detallan las librerías externas que hace uso esta aplicación.

- **PISDR014**: [Ver documentación](https://globaldevtools.bbva.com/bitbucket/projects/PE_PISD_APP-ID-26197_DSG/repos/insuranceroyal-lib14/browse/doc/PISD_APX_Library_PISDR014.md?at=refs%2Fheads%2Ffeature%2Fxp61540)
    - Métodos reutilizados: executeSignatureConstruction(String payload, String httpMethod, String uri, String queryParams, String traceId)

### 7. Mensajes de Error y Avisos:
> En este apartado se detallan los distintos mensajes de error que retornan las librerías de acuerdo a los casos y lógica de negocio implementada ...

- **Advise PISD00120009**: ERROR AL CONSULTAR EL SERVICIO DE LISTA NEGRA ASO BBVA
- **Advise PISD00120010**: ERROR AL CONSULTAR EL SERVICIO DE LISTA NEGRA SALUD RIMAC
- **Advise PISD00120011**: ERROR AL CONSULTAR EL SERVICIO DE LISTA NEGRA RIESGO RIMAC

### 8. Diseño de componentes:
# ![listRiskBlackListedEntities](images/diseño-componentes-apx-blacklist.png)