ISLL
=========

ISLL is IceScrum LDAP Layer. 
Basicly, this is my version of a huge hack to bypass the fact that icescrum
does not have LDAP connectors.

Download and install
--------------------
Download target/isll.war and simply deploy it. Then keep reading.

About
-----

As said, this is a hack. It works for me, but i cannot guarantee it will work for you.
I have a specific setup, which you might not have. 
I'll try to provide as much information as possible to allow you to port it.

Configuration
-------------

**Backup your config before deploying, or it will be overriden**

Since icescrum is in java, I assume you already have tomcat. 
If you use the self-contained server, well... sorry, i did not embbed jetty.

Assuming you have tomcat, deploy the war and open WEB-INF/ISLLConfig.xml. It should look like : 

    <config>
        <icescrumUrl>http://example.com:8080/icescrum</icescrumUrl>
        <ldapUrl>ldap://example.com:389</ldapUrl>
        <ldapBase>ou=people,dc=example,dc=com</ldapBase>
        <firstNameAttribute>gn</firstNameAttribute>
        <lastNameAttribute>sn</lastNameAttribute>
        <emailAttribute>email</emailAttribute>
        <usernameAttribute>cn</usernameAttribute>
        <principalIdentifierAttribute>uid</principalIdentifierAttribute>
        <lostPasswordUrl></lostPasswordUrl>
        <jdbcClass>com.mysql.jdbc.Driver</jdbcClass>
        <jdbcConnection>jdbc:mysql://localhost:3306/icescrum</jdbcConnection>
        <dbUsername>icescrum</dbUsername>
        <dbPassword>password</dbPassword>
        <logging>warn</logging>
        <strict>true</strict>
    </config>


* **icescrumURL** : base URL to you icescrum installation
* **ldapURL** : Anything supported by [DirContext][DirContextHref] is supported.
* **ldapBase** : Narrow searches, like -b for ldapsearch
* **firstNameAttribute** : To map your LDAP first name (given name) with icescrum. Leave blank to skip.
* **lastNameAttribute** : Same for last name
* **emailAttribute** : Same for email
* **usenameAttribute** : Attribute used to identify the user
* **principalIdentifierAttribute** : This will be used as the "principal" field to authenticate the user.§
* **listPasswordUrl** : If provided, the lost password? link will be shown
* **jdbcClass** : Currently only mysql is supported
* **jdbcConnection** : JDBC connection string
* **dbUsername** : Username to connect to the DB. Leave blank for none.
* **dbPassword** : Only used if dbUsername is not blank
* **logging** : Logging level (those supported by log4j)
* **strict** : If true, the user will have to be in your LDAP. Otherwise, it will to authenticate against LDAP and if it fails, it will redirect to icescrum's login.

§ Actually, it will be  principalIdentifierAttribute=<username>,ldapBase

Nginx
-----

I have only done an nginx config file. I'm sure you can do the same with tomcat or apache. Feel free to post it if you 
come up with one that's working. Here is nginx config : 

* ISLL running on isll.example.com
* Icescrum running on icescrum.example.com

For isll, just use proxy_pass to tomcat.
For icescrum, add this : 

    location ~* ^/icescrum/login$ {
        proxy_set_header  X-Real-IP  $remote_addr;
        proxy_set_header  X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header  Host $http_host;
        proxy_redirect    off;

        if ($args ~ norewrite) {
            proxy_pass http://127.0.0.1:8080;
            break;
        }

        rewrite ^/icescrum/login(.*)$ http://isll.openagix.com/isll$1 break;
    }

The norewrite condition will be used for the no-javascript fallback. If you don't want it, remove the if()
Adjust proxy config accordingly

Test environment
-----------------

* Tomcat 6 and 7
* Icescrum 2 #R4 with mysql
* Nginx 1.0.14
* Mysql 5
* Chromium 

Most likely this will work too with other configs... but i haven't tested it.

Features
--------

* Syncs ldap <-> icescrum users
* If user exists in ldap and not in icescrum, it will be added. Else, it is updated.
* Graceful fallback for browsers with no javascript
* Strict mode to allow only ldap users to log in.
* This script will only add or update users, it will never delete one due to issues that could arise. I let icescrum do this, so you will have to delete your users in icescrum manually

More
----

I am publishing this in the hope it can be useful to someone else out there.
However, I have developed this to suit my own needs, and there is alot to do
to improve it.
If I see interest in this, I will update it and make it more configurable.
Any feedback is appreciated


[DirContextHref]: http://docs.oracle.com/javase/1.4.2/docs/api/javax/naming/directory/DirContext.html
