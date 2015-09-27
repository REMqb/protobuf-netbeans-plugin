**This project is looking for new maintainer/owner.**
If you are interested - drop me an email: piotr.tabor@gmail.com

# Netbeans IDE plugin to support Protocol Buffers #

Table of Contents:


# Releases #

Current version of the plugin is: [2.0.0 (with protobuf-generator patched to 2.0.2)](http://code.google.com/p/protobuf-netbeans-plugin/wiki/Changelog?ts=1251312571&updated=Changelog#Version_2.0.0_(released_2009-08-26)).

Look at the [Changelog](Changelog.md) to track the history of the plugin.

# News #

## 2010-06-21 - Version 2.1.0 has been just released ##
  * Supports Netbeans 6.9
  * Added protobuf 2.3.0

## 2009-08-26 - Version 2.0.0 has been just released ##

The plugin supports integration with the project's build lifecycle.
You can set (in project's properties) that the protobuf sources should be generated
just before compilation of the project. The generated sources are stored in a special target directories (supported by netbeans 6.7).

![http://protobuf-netbeans-plugin.googlecode.com/svn/site/img/buildIntegration.jpeg](http://protobuf-netbeans-plugin.googlecode.com/svn/site/img/buildIntegration.jpeg)

See [changelog ](http://code.google.com/p/protobuf-netbeans-plugin/wiki/Changelog?ts=1251312571&updated=Changelog#Version_2.0.0_(released_2009-08-26)) for more details.

# About Protocol Buffers #

"Protocol Buffers are a way of encoding structured data in an efficient yet extensible format. Google uses Protocol Buffers for almost all of its internal RPC protocols and file formats." `[` Quoted from http://code.google.com/p/protobuf/ and go there to learn more `]`.

# Features #
## `*`.proto files edytor with syntax checking and context aware code completion ##

![http://protobuf-netbeans-plugin.googlecode.com/svn/site/img/contextCodeCompletion.jpeg](http://protobuf-netbeans-plugin.googlecode.com/svn/site/img/contextCodeCompletion.jpeg)

## Error marks in the editor ##

![http://protobuf-netbeans-plugin.googlecode.com/svn/site/img/errorMarking.jpeg](http://protobuf-netbeans-plugin.googlecode.com/svn/site/img/errorMarking.jpeg)

## Syntax navigation ##

![http://protobuf-netbeans-plugin.googlecode.com/svn/site/img/navigator.jpeg](http://protobuf-netbeans-plugin.googlecode.com/svn/site/img/navigator.jpeg)

## Simply Java code from `*`.proto files generation ##

![http://protobuf-netbeans-plugin.googlecode.com/svn/site/img/regenerate1.jpeg](http://protobuf-netbeans-plugin.googlecode.com/svn/site/img/regenerate1.jpeg)

![http://protobuf-netbeans-plugin.googlecode.com/svn/site/img/regenerateMulti.jpeg](http://protobuf-netbeans-plugin.googlecode.com/svn/site/img/regenerateMulti.jpeg)

## `*`.proto file templates ##

![http://protobuf-netbeans-plugin.googlecode.com/svn/site/img/template.jpeg](http://protobuf-netbeans-plugin.googlecode.com/svn/site/img/template.jpeg)


# Requirements #
  * Installed google protocol buffers: http://code.google.com/p/protobuf/
  * Netbeans 6.5: http://www.netbeans.org/community/releases/65/

# Installation #

<Speedy Gonzales Version>
> Update site URL is: http://protobuf-netbeans-plugin.googlecode.com/svn/distr/latest_stable/updates.xml
</Speedy Gonzales Version>

1. Install Protocol Buffers: http://code.google.com/p/protobuf/downloads/list

2. Run NetBeans IDE (6.5+)

3. Select Menu: Tools -> Plugins

4. Select "Settings" tab

5. Click "Add" button

6. Enter name: "Protobuf plugin update center" and URL: http://protobuf-netbeans-plugin.googlecode.com/svn/distr/latest_stable/updates.xml

7. Go to "Available plugins" tab

8. Select:
  * `ProtobufTemplates`
  * `ProtobufGenerator`
  * `ProtobufConfiguration`
  * `ProtobufEditor`

9. Click "Install" button

10. Close "Plugins" window

11. Select menu "Tools" -> "Options"

12. Select "Miscellaneous" tab

13. Select "Protocol Buffers" tab

![http://protobuf-netbeans-plugin.googlecode.com/svn/site/img/options.jpeg](http://protobuf-netbeans-plugin.googlecode.com/svn/site/img/options.jpeg)

14. Click "Browse" button

15. Navigate to your protoc(.exe on windows) executable

16. OK.

17. Enjoy.

# Contribution #

Fill free to join the project. Send me e-mail if you wish: name.surname@gmail.com. My name is Piotr and surname Tabor. Go there to learn more: http://piotr.tabor.waw.pl