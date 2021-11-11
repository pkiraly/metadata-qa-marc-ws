# QA Catalogue web service

This web service is a Java web application, which reads one or a small number of MARC records, analyses them, and 
displays issues it found. This application is just a lightweight wrapper around the [QA catalogue](https://github.com/piraly/metadata-qa-marc). 

At https://YOURSERVER/ws there is a web form.

The REST API endpoint is available at https://YOURSERVER/ws/validate

You can use the following parameters (see more details [here](https://github.com/pkiraly/metadata-qa-marc#validating-marc-records)):

* `marcVersion` (optional, String, default: "MARC21") MARC version. Other options: DNB, OCLC, GENT, SZTE, FENNICA, NKCR, BL, MARC21NO, UVA, B3KAT
* `marcFormat` (optional, String, default value: "XML") The input file format. Other options: ISO, XML ALEPHSEQ, LINE_SEPARATED, MARC_LINE
* `details` (optional, boolean)
* `trimId` (optional, boolean)
* `summary` (optional, boolean)
* `outputFormat` (optional, String, defaultValue = "csv") The output format. Other options: tsv, tab-separated, csv, comma-separated, text, txt, json
* `defaultRecordType` (optional, String, defaultValue = "BOOKS") The record type to use if the record's own value is invalid. Other options: 
    CONTINUING_RESOURCES, MUSIC, MAPS, VISUAL_MATERIALS, COMPUTER_FILES, MIXED_MATERIALS
* `content` (optional, String) The MARC record(s) as a string
* `file` (optional, file) The MARC record(s) in a file

Validate a binary marc file in pure MARC21 schema:
```
curl -X POST \
     -F 'marcVersion=MARC21' \
     -F 'marcFormat=ISO' \
     -F 'details=false' \
     -F 'trimId=false' \
     -F 'summary=false' \
     -F 'format=csv' \
     -F 'defaultRecordType=BOOKS' \
     -F file=@/path/to/records.mrc \
     http://YOURSERVER/ws/validate
```

Validate an alephseq file in with data elements defined in the Fennica catalogue:

```
curl -X POST \
     -F 'marcVersion=MARC21' \
     -F 'marcFormat=ALEPHSEQ' \
     -F 'details=false' \
     -F 'trimId=false' \
     -F 'summary=false' \
     -F 'format=csv' \
     -F 'defaultRecordType=BOOKS' \
     -F file=@/path/to/records.alephseq \
     http://YOURSERVER/ws/validate
```


## How to run

This webservice can be launched in two ways. At the end of the process the service will be accessible from port 8080 
(so as http://localhost:8080/ws and http://YOURSERVER:8080/ws)

For the time being the service creates some temporary files. Please create a directory, which is writable to the user 
who runs the process (e.g. if you run the application in Tomcat, it is Tomcat user).

```
sudo mkdir /tmp/ws
sudo chown Tomcat /tmp/ws
sudo chmod u+w /tmp/ws
```

(Later the directory will be configurable, but now it is not. Sorry for the inconvenience.)

### 1. Run in a standalone Spring Boot web server
```
nohup mvn spring-boot:run &
```

### 2. Run in a Java web server, such as Apache Tomcat

In this section I suppose you use Ubuntu, and installed tomcat with the the package manager (`sudo apt install tomcat9`). 
Depending your mode of installation the way to stop/start Tomcat and where is Tomcat's webapps directory might be different.

1. build the application
```
mvn clean package
```

2. Stop Tomcat
```
sudo service tomcat9 stop
```

3. deploy the application
```
cp target/ws.war /var/lib/tomcat9/webapps/
```
Note: Tomcat might be in a different location in your server, the important part is that you should put it into the `webapps` directory.

4. Start Tomcat
```
sudo service tomcat9 start
```

### Make it accessible at port 80

In most of the system port 8080 is not available from outside, so you have to change the webserver to make a proxy, which let
users access it via port 80. The following description will use Apache https server on Ubuntu machine.

1. Install proxy modules

```
sudo a2enmod proxy
sudo a2enmod proxy_http
```

2. edit main Apache conf
```
sudo nano /etc/apache2/apache2.conf
```
add the following line

```
LoadModule proxy_module modules/mod_proxy.so
```

3. edit the site configuration files

```
sudo nano /etc/apache2/sites-available/default-ssl.conf
sudo nano /etc/apache2/sites-available/000-default.conf
```
add the following lies:

```
ProxyRequests off
<Proxy *>
  Order deny,allow
  Allow from all
</Proxy>

ProxyPass /ws http://localhost:8080/ws connectiontimeout=300 timeout=300
```

4. restart the webserver

```
sudo service apache2 restart
```

