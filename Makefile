all:	build run
	
build:
	@echo "Building..."
	mvn package

run:	
	@java -cp target/A0A1-1.0-SNAPSHOT.jar com.shreysa.app.App

