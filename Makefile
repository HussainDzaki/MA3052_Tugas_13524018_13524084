.PHONY: run

UUID = 374900b6-8eb2-4f13-9e2f-475f66f8b032

run:
	mvn clean javafx:run

package:
	mvn clean package

build-msi: package
	jpackage --input target/ \
			 --dest bin/ \
			 --type msi	\
			 --name GraphVisualization \
			 --module-path "target/visualisasigraf-1.0.jar;target/libs" \
	  		 --module ma3052/ma3052.Launcher \
			 --win-shortcut \
	  		 --win-menu \
			 --win-dir-chooser \
			 --win-shortcut-prompt \
			 --win-upgrade-uuid ${UUID}

build-exe: package
	jpackage --input target/ \
			 --dest bin/ \
			 --type exe	\
			 --name GraphVisualization \
			 --module-path "target/visualisasigraf-1.0.jar;target/libs" \
	  		 --module ma3052/ma3052.Launcher \
			 --win-shortcut \
	  		 --win-menu \
			 --win-dir-chooser \
			 --win-shortcut-prompt \
			 --win-upgrade-uuid ${UUID}
	  