.PHONY: run

JPACKAGE_FLAGS = --input target/ --dest bin/ --name GraphVisualization --icon favicon.ico \
				 --module-path "target/visualisasigraf-1.0.jar;target/libs" \
	  		 	 --module ma3052/ma3052.Launcher
			 		

UUID = 374900b6-8eb2-4f13-9e2f-475f66f8b032

WINDOW_FLAGS = --win-shortcut \
	  		   --win-menu \
			   --win-dir-chooser \
			   --win-shortcut-prompt \
			   --win-upgrade-uuid ${UUID}

run:
	mvn clean javafx:run

package:
	mvn clean package

build-msi: package
	jpackage --type msi	${JPACKAGE_FLAGS} ${WINDOW_FLAGS}
			 

build-exe: package
	jpackage --type exe	${JPACKAGE_FLAGS} ${WINDOW_FLAGS}

build-dmg: package
	jpackage --type dmg	${JPACKAGE_FLAGS}

build-deb: package
	jpackage ${JPACKAGE_FLAGS}
	  