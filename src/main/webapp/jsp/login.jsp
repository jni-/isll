<%@ page import="com.jnispace.isll.config.ISLLConfig" %>
<%@ page import="com.jnispace.isll.facade.dto.ResponseDTO" %>
<%
	ISLLConfig isllConfig = (ISLLConfig) request.getAttribute("config");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<!--[if IE 8]><meta http-equiv="X-UA-Compatible" content="IE=8"/><![endif]-->
		
		<link href="static/css/css1.css" type="text/css" rel="stylesheet" media="screen,projection">
		<link href="static/css/css2.css" type="text/css" rel="stylesheet" media="screen,projection">
		<link href="static/css/css3.css" type="text/css" rel="stylesheet" media="screen,projection">

		<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>
		<script src="static/js/jquery.form.js"></script>
		<script src="static/js/jquery.blockUI.js"></script>
		
		<script>
			$(document).ready(function() {
				var options = {
					beforeSerialize: showOverlay,
					success: handleResponse,
					dataType: "json"
				};
				
				$('#loginForm').ajaxForm(options);
			});
			
			function showOverlay() {
				$.blockUI({ css: { 
					            border: 'none', 
					            padding: '15px', 
					            backgroundColor: '#000', 
					            '-webkit-border-radius': '10px', 
					            '-moz-border-radius': '10px', 
					            opacity: .5, 
					            color: '#fff' 
        						} 
        		});
        	} 
			
			function handleResponse(data) {
				if(data != undefined && (data.type == "<%= ResponseDTO.SUCCESS %>" || data.type == "<%= ResponseDTO.PASS_THROUGH %>")) {
					$('#loginForm').attr('action', "<%= isllConfig.icescrumUrl %>/j_spring_security_check");
					$('#loginForm').unbind('submit');
					$('#loginForm').die('submit');
					$('#loginForm').submit();
				} else if (data != undefined) {
					$('.messages').remove();
					$('<div class="messages"><p class="error"><strong>An error occured</strong><br />' + data.message + '</p></div>').prependTo('#login_form');
				}
				$.unblockUI();
			}
			
		</script>
  		<meta name="layout" content="simple">
	</head>
<body class="simple">



<div id="main-simple"><div id="main-content">
  <div id="login_form" class="box">

	<div class="messages">
		<%
			if(request.getAttribute("responseDTO") != null) {
			    ResponseDTO dto = (ResponseDTO) request.getAttribute("responseDTO");
			    if(ResponseDTO.SUCCESS.equals(dto.type) && ResponseDTO.PASS_THROUGH.equals(dto.type)) {
			        %>
			        	<p class="sucess">
			        		Login successful. However, it seems that your browser does not have javascript enabled. <br />
			        		<a href="<%= isllConfig.icescrumUrl %>/login?norewrite">Please click here and log in again</a>
			        	</p>
			        <%
			    } else {
			        %>
			        	<p class="error">
			        		<strong>An error occured</strong> <br />
			        		<span id="error-message"><%= dto.message %></span>
			        	</p>
			        <%
			    }
			}
		
			String icescrumError = request.getParameter("login_error");
			if(icescrumError != null && icescrumError.equals("1")) {
			    %>
			    <p class="error">
			    	<strong>An error occured</strong>
			    	<span id="error-message">This is not a valid icescrum user.</span>
			    </p>
			    <%
			}
		%>
	</div>

    <div class="box-title"><span class="start"></span><p class="content">Login - ISLL</p><span class="end"></span></div>

    <form method="POST" id="loginForm" name="loginForm" class="box-form box-form-small-legend box-content box-form-160">

      <div class="field-information field-information-nobordertop">
        <div class="welcome">
          Welcome to iceScrum!
        </div>
        
        <%
        	if(isllConfig.lostPasswordUrl.length() > 0) {
        		%>
		        <div class="retrieve-link">
		            	<a href="<%= isllConfig.lostPasswordUrl %>">
		              		Lost password ?
		            	</a>
		        </div>
		        <%
        	}
        %>
      </div>

      <p class="field-input clearfix"><label for="username">Username</label>
        <span id="username-field" class="input"><span class="start"></span><span class="content"><input type="text" name="j_username" id="username" value="" focus="true"></span><span class="end"></span></span><script type="text/javascript">jQuery(function(){$('#username').focus();}); </script>
      </p>

      <p class="field-input clearfix"><label for="password">Password</label>
        <span class="input" id="password-field"><span class="start"></span><span class="content"><input type="password" name="j_password" id="password" value=""></span><span class="end"></span></span>
      </p>

      <p class="field-check-line clearfix">
        <input type="checkbox" name="_spring_security_remember_me" id="remember_me">
        <label for="remember_me">Remember me</label>
      </p>
      
      <div class="field-buttons" id="login-button-bar"><table cellpadding="0" cellspacing="0" border="0"><tbody><tr><td width="50%">&nbsp;</td>
          <td></td>
          <td>
          	<span class="button-s clearfix">
          		<span class="start" onclick="$(this).closest('form').submit();"></span>
          		<span class="content" onclick="$(this).closest('form').submit();">Connect</span>
          		<span class="end" onclick="$(this).closest('form').submit();"></span>
          		<span class="mask-submit"><input type="submit" id="loginSubmit"></span>
          	</span>
          </td>
      	<td width="50%">&nbsp;</td></tr></tbody></table></div>

    </form>
  </div>
</div></div>

</body>
</html>