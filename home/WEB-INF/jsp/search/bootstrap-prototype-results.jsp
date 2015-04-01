
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title></title>
    <link href="/css/bootstrap/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<script src="/css/bootstrap/js/bootstrap.min.js"></script>


</body>
</html>



<html lang="en">
  <head>
    <meta charset="utf-8">
    <title>Bootstrap, from Twitter</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <!-- Le styles -->
      <link href="/css/bootstrap/css/bootstrap.min.css" rel="stylesheet">

    <style>
      body {
        padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */
      }
    </style>
    <link href="/css/bootstrap/css/bootstrap-responsive.css" rel="stylesheet">

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
      <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

    <!-- Le fav and touch icons -->
    <link rel="shortcut icon" href="/css/bootstrap/ico/favicon.ico">
    <link rel="apple-touch-icon-precomposed" sizes="144x144" href="bootstrap/ico/apple-touch-icon-144-precomposed.png">
    <link rel="apple-touch-icon-precomposed" sizes="114x114" href="bootstrap/ico/apple-touch-icon-114-precomposed.png">
    <link rel="apple-touch-icon-precomposed" sizes="72x72" href="bootstrap/ico/apple-touch-icon-72-precomposed.png">
    <link rel="apple-touch-icon-precomposed" href="bootstrap/ico/apple-touch-icon-57-precomposed.png">
  </head>

  <body>

    <div class="navbar navbar-fixed-top">
      <div class="navbar-inner">
        <div class="container">
          <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </a>
          <a class="brand" href="#">ZFIN</a>
          <div class="nav-collapse">
            <ul class="nav">
              <li class="active"><a href="#">Home</a></li>
              <li><a href="#about">About</a></li>
              <li><a href="#contact">Contact</a></li>
            </ul>
          </div><!--/.nav-collapse -->
        </div>
      </div>
    </div>

    <div class="container-fluid">
      <div class="row">
		<div class="col-md-12">
         <form class="form-search">
              <div class="input-append">
                <input class="col-md-8" id="appendedInputButton" type="text"><button class="btn" type="button">Search</button>
                <a href="#" onclick="jQuery('#advanced-search').slideToggle();">Advanced</a>
             </div>
         </form>
         <form id="advanced-search" class="form-horizontal" style="display:none;">
			 <div>
				 <select>
					 <option>Gene Symbol</option>
				     <option>Publication Abstract</option>
					 <option>Phenotype Anatomy</option>
			     </select>
			     <select><option>Contains</option><option>Starts With</option><option>Equals</option></select>
			     <input class="col-md-6" type="text">
			 </div>
			 <div>
				 <select>
                	 <option>Gene Symbol</option>
				     <option selected="true">Publication Abstract</option>
					 <option>Phenotype Anatomy</option>
				 </select>
				 <select><option>Contains</option><option>Starts With</option><option>Equals</option></select>
			     <input class="col-md-6" type="text"></div>
 			 <div>
				 <select>
					 <option>Gene Symbol</option>
				     <option>Publication Abstract</option>
					 <option selected="true">Phenotype Anatomy</option>
				 </select>
				 <select><option>Contains</option><option>Starts With</option><option>Equals</option></select>
				 <input class="col-md-6" type="text">
				 <button class="btn" type="button">Search</button>
			 </div>

         </form>
       </div>

      </div>
      <div class="row">
          <div class="col-md-3 well sidebar-nav">
            <ul class="nav nav-list">
              <li class="nav-header">Sidebar</li>
              <li><a href="#">Link</a></li>
              <li><a href="#">Link</a></li>
              <li><a href="#">Link</a></li>
              <li><a href="#">Link</a></li>
              <li class="nav-header">Sidebar</li>
              <li><a href="#">Link</a></li>
              <li><a href="#">Link</a></li>
              <li><a href="#">Link</a></li>
              <li><a href="#">Link</a></li>
              <li><a href="#">Link</a></li>
              <li><a href="#">Link</a></li>
              <li class="nav-header">Sidebar</li>
              <li><a href="#">Link</a></li>
              <li><a href="#">Link</a></li>
              <li><a href="#">Link</a></li>
              <li class="nav-header">Start Stage</li>
              <li><form><input name="startstage" type="range" min="1" max="50" value="1" onchange="startstageoutput.value = 'Stage ' + startstage.value;"/>
                        <output class="pull-right label label-info" name="startstageoutput">Stage 1</output>
                  </form></li>

              <li class="nav-header">End Stage</li>
              <li><form><input name="endstage" type="range" min="1" max="50" value="50" onchange="endstageoutput.value = 'Stage ' + endstage.value;"/>
                        <output class="pull-right label label-info" name="endstageoutput">Stage 50</output>
                  </form></li>
            </ul>
          </div><!--/.well -->
        <div class="col-md-9 well">
              a place for results or whatever
        </div>

      </div> <!-- row -->
    </div> <!-- /container -->

    <!-- Le javascript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="/css/bootstrap/js/jquery.js"></script>
    <script src="/css/bootstrap/js/bootstrap-transition.js"></script>
    <script src="/css/bootstrap/js/bootstrap-alert.js"></script>
    <script src="/css/bootstrap/js/bootstrap-modal.js"></script>
    <script src="/css/bootstrap/js/bootstrap-dropdown.js"></script>
    <script src="/css/bootstrap/js/bootstrap-scrollspy.js"></script>
    <script src="/css/bootstrap/js/bootstrap-tab.js"></script>
    <script src="/css/bootstrap/js/bootstrap-tooltip.js"></script>
    <script src="/css/bootstrap/js/bootstrap-popover.js"></script>
    <script src="/css/bootstrap/js/bootstrap-button.js"></script>
    <script src="/css/bootstrap/js/bootstrap-collapse.js"></script>
    <script src="/css/bootstrap/js/bootstrap-carousel.js"></script>
    <script src="/css/bootstrap/js/bootstrap-typeahead.js"></script>

  </body>
</html>
