# ![Logo-template](images/logo-template.png)
# Library PISDR018

> El objetivo de este documento es proveer información relacionada a la librería PISDR018 que utiliza la transacción PISDT005 y que ha sido implementado en APX.

### 1. Funcionalidad:
> Esta Librería APX tiene como objetivo realizar la lógica de negocio de la transacción PISDT005.

#### 1.1 Caso de Uso:

> El uso de la Librería PISDR015 está orientado a realizar los mapeos de los campos de salida de la transacción, realizar validaciones, todo lo necesario para cumplir con la lógica de negocio.

### 2. Capacidades:

> Esta **librería** brinda la capacidad de poder ejecutar la lógica de negocio de la transacción de lista negra (PISDT005) de forma fácil y segura con el siguiente método:


#### 2.1 Método 1: executeBlackListValidation(InsuranceBlackListDTO input)
> Método que ejecuta toda la lógica de negocio

##### 2.1.1 Datos de Entrada

|#|Nombre del Atributo|Tipo de Dato| Descripción|
| :----|:---------- |:--------------| :-----|
|1| InsuranceBlackListDTO | Object | Dto que contiene el cuerpo de la solicitud |
|1.1| customerId | String | Código del cliente |
|1.2| identityDocument | Object | Objeto documento de identidad |
|1.2.1| documentType | Object | Objeto tipo de documento |
|1.2.1.1| id | String | Código del tipo de documento |
|1.2.2| documentNumber | String | Número de documento |
|1.3| blackListType | Object | Objeto tipo de lista negra |
|1.3.1| id | String | Código del tipo de lista negra |
|1.4| blockingCompany | Object | Objeto compañia bloqueante |
|1.4.1| id | String | Código de la compañia bloqueante |
|1.5| product | Object | Objeto producto |
|1.5.1| id | String | Código del producto |

##### 2.1.2 Datos de Salida

|#|Nombre del Atributo|Tipo de Dato| Descripción|
| :----|:---------- |:--------------| :-----|
|1| EntityOutBlackListDTO | Object | Dto que contiene la respuesta del servicio |
|1.1| data | List | Lista que contiene información de la evaluación |
|1.1.1| id | String | Código |
|1.1.2| isBlocked | String | Valor que determina si esta bloqueado |
|1.1.3| identityDocument | Object | Objeto documento de identidad |
|1.1.3.1| documentType | Object | Objeto tipo de documento |
|1.1.3.1.1| id | String | Código tipo de documento |
|1.1.3.2| number | String | Número de documento |
|1.1.4| blockType | Object | Objeto tipo de bloqueo |
|1.1.4.1| id | String | Código tipo de bloqueo |
|1.1.5| description | String | Descripción de la evaluación |
|1.1.6| blackListType | Object | Objeto tipo de lista negra |
|1.1.6.1| id | String | Código del tipo de lista negra |
|1.1.7| entryDate | Date | Fecha de entrada |

##### 2.1.3 Ejemplo
```java
EntityOutBlackListDTO blvalidation = pisdR018.executeBlackListValidation(InsuranceBlackListDTO input);
```

### 3.  Mensajes:

    Ningún mensaje

### 4.  Versiones:
#### 4.1  Versión 0.11.0-SNAPSHOT

+ Versión 0.11.0-SNAPSHOT: Esta versión permite realizar la lógica de negocio para cumplir con el proceso deseado de la transaccion PISDT005.