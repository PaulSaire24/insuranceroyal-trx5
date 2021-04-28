# ![Logo-template](images/logo-template.png)
# Library PISDR008

> El objetivo de este documento es proveer información relacionada a la librería PISDR008 que utiliza la transacción PISDT005 y que ha sido implementado en APX.

### 1. Funcionalidad:

> Esta Librería APX tiene como objetivo consumir servicios tanto internos (BBVA) como externos (Rimac).

#### 1.1 Caso de Uso:

> EL uso de la Librería PISDR008 está orientado a consumir el servicio listCustomerIndicators del API customers,
>  el servicio de Riesgo persona de Rimac y el servicio de Riesgo salud de Rimac.

### 2. Capacidades:
> Esta **librería** brinda la capacidad de poder consumir los servicios mencionados de forma segura y fácil mediante los siguientes métodos:

#### 2.1 Método 1: executeGetBlackListIndicatorService(String customerId)
> Método para listar los indicadores de un cliente.

##### 2.1.1 Datos de Entrada

    Ninguno, solo se envía como path param el parámetro de entrada del método - "customerId"

##### 2.1.2 Datos de Salida

|#|Nombre del Atributo|Tipo de Dato| Descripción|
| :----|:---------- |:--------------| :-----|
|1| BlackListIndicatorBO | Object | Objeto que contiene la respuesta del servicio |
|1.1| data | List | Lista que contiene datos del cliente |
|1.1.1| indicatorId | String | Código de indicador |
|1.1.2| name | String | Nombre del indicador |
|1.1.3| isActive | Boolean | Determina si está o no activo |

##### 2.1.3 Ejemplo
```java
BlackListIndicatorBO indicator = pisdR008.executeGetBlackListIndicatorService(String customerId);
```

#### 2.2 Método 2: executeGetBlackListRiskService(IdentityDataDTO input, String traceId)
> Método para obtener información del cliente, si tiene riesgo vehicular.

##### 2.2.1 Datos de Entrada

|#|Nombre del Atributo|Tipo de Dato| Descripción|
| :----|:---------- |:--------------| :-----|
|1| IdentityDataDTO | Object | Objeto que contiene el cuerpo de la solicitud |
|1.1| tipoLista | String | Tipo de lista |
|1.2| tipoDocumento | String | Código del tipo de documento |
|1.3| nroDocumento | String | Número de documento |
|2| traceId | String | Código de la trama |

##### 2.2.2 Datos de Salida

|#|Nombre del Atributo|Tipo de Dato| Descripción|
| :----|:---------- |:--------------| :-----|
|1| SelectionQuotationPayloadBO | Object | Objeto que contiene la respuesta del servicio |
|1.1| status | String | Código de estado |
|1.2| mensaje | String | Mensaje de evaluación |


##### 2.2.3 Ejemplo
```java
SelectionQuotationPayloadBO resp = pisdR008.executeGetBlackListRiskService(IdentityDataDTO input, String traceId);
```

#### 2.3 Método 3: executeGetBlackListHealthService(IdentityDataDTO input, String traceId)
> Método para obtener información de cliente, si tiene riesgo en temas de salud.

##### 2.3.1 Datos de Entrada

|#|Nombre del Atributo|Tipo de Dato| Descripción|
| :----|:---------- |:--------------| :-----|
|1| IdentityDataDTO | Object | Objeto que contiene el cuerpo de la solicitud |
|1.1| tipoDocumento | String | Código del tipo de documento |
|1.2| nroDocumento | String | Número de documento |
|2| traceId | String | Código de la trama |

##### 2.3.2 Datos de Salida

|#|Nombre del Atributo|Tipo de Dato| Descripción|
| :----|:---------- |:--------------| :-----|
|1| SelectionQuotationPayloadBO | Object | Objeto que contiene la respuesta del servicio |
|1.1| status | String | Código de estado |
|1.2| mensaje | String | Mensaje de evaluación |

##### 2.3.3 Ejemplo
```java
SelectionQuotationPayloadBO resp = pisdR008.executeGetBlackListHealthService(IdentityDataDTO input, String traceId);
```

### 3.  Mensajes:

#### 3.1  Código PISD00120009:
> Este código de error es devuelto cuando se presenta un error al momento de consumir el servicio listCustomerIndicators.

#### 3.2  Código PISD00120010:
> Este código de error es devuelto cuando se presenta un error al momento de consumir el servicio lista negra salud Rimac.

#### 3.3  Código PISD00120011:
> Este código de error es devuelto cuando se presenta un error al momento de consumir el servicio lista negra riesgo Rimac.


### 4.  Versiones:
#### 4.1  Versión 0.11.0-SNAPSHOT

+ Versión 0.11.0-SNAPSHOT: Esta versión permite consumir los servicios mencionados, 1 interno BBVA y 2 externos de Rimac.