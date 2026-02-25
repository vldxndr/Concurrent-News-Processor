JAVAC = javac
JAVA  = java

# directorul cu biblioteci .jar
LIB_DIR = ./libs

# classpath pentru Jackson
CLASSPATH = .:$(LIB_DIR)/jackson-core-3.0.2.jar:$(LIB_DIR)/jackson-databind-3.0.3.jar:$(LIB_DIR)/jackson-annotations-3.0-rc5.jar

SOURCES := $(wildcard *.java)

MAIN_CLASS = Tema1

build:
	$(JAVAC) -cp $(CLASSPATH) $(SOURCES)

run:
	$(JAVA) -cp $(CLASSPATH) $(MAIN_CLASS) $(ARGS)

clean:
	find . -name "*.class" -type f -delete

.PHONY: build run clean