[![Status](https://github.com/giis-uniovi/modevo/actions/workflows/test.yml/badge.svg)](https://github.com/giis-uniovi/modevo/actions)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=my%3Amodevo&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=my%3Amodevo)
# MoDEvo - Model-driven approach to maintain data integrity for evolution of column family DBMSs

This repository contains the modules of the Model-driven engineering approach MoDEvo, which provides the data migrations required to maintain data integrity in a column family DBSM when the database evolves:

- Provide a data migration model given an evolution of the schema.
- Transformation of the data migration model in a script.
- Execution of the script against a Cassandra database.
- Available for Java 17 and higher.

## Quick Start

Currently, MoDEvo can be used by importing it as a Maven project in Eclipse. MoDEvo transforms the the input models of MoDEvo (Schema, Schema Evolution and Conceptual Model) requires into a Data Migration model by executing ATL transformations. There are two possible ways to execute these transformations:

- Using the ATL module: Run from Eclipse the MoDEvo.atl as an ATL transformation. In the configuration menu, you must insert the paths for the inputs models and the path where the output model will be generated. It is required to install the last version of the ATL module in Eclipse (https://download.eclipse.org/mmt/atl/updates/releases/)

- Using Java: Create a Main Java class or a test case using JUnit that imports the class `MainTransformations` and then call the method `createDataMigrationModelAndScript` with the corresponding input models paths and the output model path.

<details open><summary><strong>Java</strong></summary>

```Java Main class
import giis.modevo.transformations.MainTransformations;
public static void main (String[] args){
   	new MainTransformations().createDataMigrationModelAndScript("pathSchema.xml", "pathConceptuaModel.xml", "pathSchemaEvolution.xml", "outputPath.xml");
 }
```
```Java Test Case
@Test
	public void testExample () {
		   	new MainTransformations().createDataMigrationModelAndScript("pathSchema.xml", "pathConceptuaModel.xml", "pathSchemaEvolution.xml", "outputPath.xml");
	}
```



</details>


## Modules

MoDEvo is composed of the following modules:

- `modevo-transform`: Transforms the input models (Schema, Schema Evolution and Conceptual Model) in a Data Migration model that specifies the data migrations to perform.
- `modevo-script`: Transforms a Data Migration model in scripts stored as Java objects that can, optionally be executed against a Cassandra database using this module. It also provides a textual representation of the script in a Cassandra Query Language-like language.

## modevo-transform module

Determines the data migrations required to maintain the data integrity in a column family DBMS . It determines these migration through the execution of ATL transformations that are located in the package `giis.modevo.transformations.atl`. In order to execute these transformations, MoDEvo requires of the following input models in xml format:

- Conceptual Model: Contains the structure of the conceptual model, specifying its entities, their relationships and the attributes that are part of an entity or relationship.
- Schema: Contains the structure of the schema, specifying its tables and the columns of these tables. It also contains the association of each column with an attribute of the schema.
- Schema Evolution: Contains the evolution performed in the schema.

Given these three inputs models, MoDEvo transforms them into a Data Migration model:

- Data Migration: Contains all the migrations that are required to maintain the data integrity.

In addition, Java objects of each of the input and output models are created to be used in the MoDEvo-script module.

## modevo-script module

Transforms the data migration model generated in the MoDEvo-transformation module into a script that can be executed against a column family DBSM. Currently it supports the most used column family DBSM, Apache Cassandra. In order to execute this transformation, MoDEvo requires the Schema, Schema Evolution and Data Migration models as Java objects.

Given these objects, MoDEvo creates one script for each table that requires data migrations in two ways:

- Textual representation in a Cassandra Query Language-like format. It is composed of the three basic opeators FOR, SELECT and INSERT.
- Optionally, the script can be executed in a Cassandra database, requiring to configure the "MoDEvo.properties" file located in the root with the appropiate values.

## modevo-consistency module

Uses a SQL database to check that MoDEvo maintain data integrity. 

Both the SQL database and the Cassandra database store initially the same data (in a normalized model for SQL and denormalized for Cassandra). Then, the data is migrated accordingly in both databases, in the SQL database with this module and in Cassandra by executing the scripts generated in the script module. Finally, both databases are compared to check if MoDEvo maintained data integrity.

## Replication package

The replication package instructions for this project is located at the following repository: https://doi.org/10.5281/zenodo.14509607

## Citing this work

TODO

## Related work

Suárez-Otero, P., Mior, M. J., Suárez-Cabal, M. J., & Tuya, J. (2023). CoDEvo: Column family database evolution using model transformations. Journal of Systems and Software, 111743.

Suárez-Otero González, P., Mior, M. J., Suárez Cabal, M. J., & Tuya González, P. J. (2023, March). Data migration in column family database evolution using MDE. In Workshop Proceedings of the EDBT/ICDT 2023 Joint Conference (March 28-March 31, 2023, Ioannina, Greece). George Fletcher and Verena Kantere.

Suárez-Cabal, M. J., Suárez-Otero, P., de la Riva, C., & Tuya, J. (2023). MDICA: Mainte-nance of data integrity in column-oriented database applications. Computer Standards & In-terfaces, 83, 103642.
