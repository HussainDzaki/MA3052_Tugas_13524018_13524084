.PHONY: run

run:
	mvn clean javafx:run

package:
	mvn clean package

build: package
	jpackage --input target/ --dest bin/ --name GraphVisualization --main-jar visualisasigraf-1.0.jar --main-class ma3052.Launcher --type exe 	