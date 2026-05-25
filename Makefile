.PHONY: run

# Windows paths use semicolons, Mac/Linux use colons
WINDOWS_MODULE_PATH = "target/visualisasigraf-1.0.jar;target/libs"
MAC_MODULE_PATH = "target/visualisasigraf-1.0.jar:target/libs"

# Base jpackage flags (without module-path and icon)
JPACKAGE_BASE = --input target/ --dest bin/ --name GraphVisualization --module ma3052/ma3052.Launcher

# Windows-specific flags
WINDOWS_FLAGS = --win-shortcut --win-menu --win-dir-chooser --win-shortcut-prompt
UUID = 374900b6-8eb2-4f13-9e2f-475f66f8b032

run:
	mvn clean javafx:run

package:
	mvn clean package

build-msi: package
	jpackage --type msi ${JPACKAGE_BASE} --module-path ${WINDOWS_MODULE_PATH} --icon favicon.ico ${WINDOWS_FLAGS} --win-upgrade-uuid ${UUID}

build-exe: package
	jpackage --type exe ${JPACKAGE_BASE} --module-path ${WINDOWS_MODULE_PATH} --icon favicon.ico ${WINDOWS_FLAGS} --win-upgrade-uuid ${UUID}

build-dmg: package
	jpackage --type dmg ${JPACKAGE_BASE} --module-path ${MAC_MODULE_PATH} --icon favicon.ico 

build-pkg: package
	jpackage --type pkg ${JPACKAGE_BASE} --module-path ${MAC_MODULE_PATH} --icon favicon.ico 
	  