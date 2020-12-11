# DBLP Pano Demo

## Prerequisites
- Java 11+
- gradle
- Pano CLI: https://docs.pano.dev/data-preparation/working-with-the-panoramic-cli/installation
- Snowflake CLI: https://docs.snowflake.com/en/user-guide/snowsql.html

## Preparation
1. Download DBLP dataset and extract to the data subfolder: https://dblp.org/faq/1474679.html
2. Create a Snowflake account
3. Register at https://pano.dev

## Loading the DBLP dataset into Snowflake
The following command will convert the DBLP dataset into JSON files for publications, persons, etc.

    gradle run

Now prepare the data model in Snowflake, by executing the commands from the Snowflake CLI, specified in the ddl.sql file.

## Connecting Snowflake to Pano

1. Set up an account at pano.dev
2. Set up the Pano CLI, by following https://docs.pano.dev/data-preparation/working-with-the-panoramic-cli
3. Run `pano push` from the project folder

## Use the DBLP data to build charts in Pano
Now it is possible to build charts and dashboards using the dataset of the DBLP data.
