import xml.etree.ElementTree as ET
import os

pom_path : str = os.getcwd() + "/pom.xml"

if not os.path.exists(pom_path):
    print("Error: pom.xml expected in current working directory")
    exit(1)

xml_root = ET.parse(pom_path).getroot()

java_version_node = xml_root.find("java.version")

if java_version_node is not None:
    java_version = java_version_node.text
