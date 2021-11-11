# QA Catalogue web service

This web service is a Java web application, which reads one or a small number of MARC records, analyses them, and 
displays issues it found. This application is just a lightweight wrapper around the [QA catalogue](https://github.com/piraly/metadata-qa-marc). 

The base address is
https://YOURSERVER/ws

You can use the following parameters

* `marcVersion` (optional, default: "MARC21")
* `marcFormat` (optional, default value: "XML")
* `details` (optional, boolean)
* `trimId` (optional, boolean)
* `summary` (optional, boolean)
* `outputFormat` (optional, defaultValue = "csv")
* `defaultRecordType` (optional, defaultValue = "BOOKS")


## How to run

This webservice can be launched in two ways. At the end of the process the service will be accessible from port 8080 
(so as http://localhost:8080/ws and http://YOURSERVER:8080/ws)


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

