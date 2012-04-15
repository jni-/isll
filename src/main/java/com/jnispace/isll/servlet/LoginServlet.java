package com.jnispace.isll.servlet;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.jnispace.isll.facade.ISLLLoginFacade;
import com.jnispace.isll.facade.dto.ResponseDTO;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;

public class LoginServlet extends ConfiguredServletBase {
    public final static String USERNAME_FIELD = "j_username";
    public final static String PASSWORD_FIELD = "j_password";

    public final static String JSP_LOGIN_PAGE_PATH = "/jsp/login.jsp";
    public final static String XML_HTTP_REQUEST_HEADER = "XMLHttpRequest";
    public final static String X_REQUESTED_WITH_HEADER = "X-Requested-With";

    public final static String RESPONSE_DTO_RQUEST_ATTRIBUTE_NAME = "responseDTO";

    public LoginServlet() {
        super(Logger.getLogger("file"));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        configure(request);
        getServletConfig().getServletContext().getRequestDispatcher(JSP_LOGIN_PAGE_PATH).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        configure(request);

        String username = request.getParameter(USERNAME_FIELD);
        String password = request.getParameter(PASSWORD_FIELD);

        ISLLLoginFacade facade = getFacade();
        ResponseDTO responseDTO = facade.authenticate(username, password);
        createResponse(request, response, responseDTO);
    }

    protected ISLLLoginFacade getFacade() {
        return new ISLLLoginFacade(getConfig());
    }

    private void createResponse(HttpServletRequest request, HttpServletResponse response, ResponseDTO responseDTO) throws IOException,
            ServletException {
        if (isAjax(request)) {
            response.getWriter().append(dtoToJson(responseDTO));
        } else {
            request.setAttribute(RESPONSE_DTO_RQUEST_ATTRIBUTE_NAME, responseDTO);
            getServletConfig().getServletContext().getRequestDispatcher(JSP_LOGIN_PAGE_PATH).forward(request, response);
        }
    }

    protected String dtoToJson(ResponseDTO responseDTO) {
        XStream xstream = new XStream(new JsonHierarchicalStreamDriver() {
            @Override
            public HierarchicalStreamWriter createWriter(Writer writer) {
                return new JsonWriter(writer, JsonWriter.DROP_ROOT_MODE);
            }
        });
        xstream.setMode(XStream.NO_REFERENCES);
        return xstream.toXML(responseDTO);
    }

    private boolean isAjax(HttpServletRequest request) {
        return XML_HTTP_REQUEST_HEADER.equals(request.getHeader(X_REQUESTED_WITH_HEADER));
    }

    protected LoginServlet(Logger logger) {
        super(logger);
    }

    private static final long serialVersionUID = 1L;

}
