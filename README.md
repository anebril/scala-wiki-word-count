# scala-wiki-word-count
Educational application to illustrate usage of scala, maven and derby (counts words present in czech wiki page). Application accepts search query and loads corresponding wiki article from czech (or specified) wikipedia page. Application analyzes word occurrences in loaded article. Analyzed occurrences will be printed to standard output.

Each result of analysis is cached in runtime derby database. Application doesn't perform wikipedia request when there is cached result in database.

Program uses wikipedia api for loading articles. For more information please see https://www.mediawiki.org/wiki/API:Main_page

## Build

Program is built using maven compiler on project pom file. Final jar artifact will be placed in target folder in project source directory. Program can be built using mvn command in project source folder:

### Perform tests and builds final jar

    mvn install

### Builds final jar without tests

    mvn install -DskipTests

## Usage

Application is run by invoking generated jar file using java command line tool. Application accepts single parameter which specifies search query (prints usage if no parameter is specified). Result of the program is printed to standard output.

Optionally user may specify language version of wikipedia by setting text.source property in java command. Application uses czech wikipedia by default.

### Load and analyze Scala wiki page from czech wikipedia

    java -jar target/scala-wiki-word-count-1.0-SNAPSHOT.jar "Scala"

### Load and analyze Scala wiki page from english wikipedia

    java -Dtext.source=en.wikipedia.org -jar target/scala-wiki-word-count-1.0-SNAPSHOT.jar "Scala"

