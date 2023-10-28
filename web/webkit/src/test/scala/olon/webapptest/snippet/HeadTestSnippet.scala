package olon
package webapptest
package snippet

class HeadTestSnippet {
  def withHead = {
    <div>
    <head>
    <script type="text/javascript" src="snippet.js"></script>
    </head>
    <span>Welcome to webtest1 at {new java.util.Date}</span>
    </div>
  }

}
