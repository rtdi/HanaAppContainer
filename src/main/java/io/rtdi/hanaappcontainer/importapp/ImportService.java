package io.rtdi.hanaappcontainer.importapp;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.rtdi.hanaappcontainer.designtimeobjects.hdbtable.Actions;
import io.rtdi.hanaappcontainer.designtimeobjects.hdbtable.HDBTable;
import io.rtdi.hanaappserver.hanarealm.HanaPrincipal;
import io.rtdi.hanaappserver.utils.ErrorMessage;
import io.rtdi.hanaappserver.utils.HanaSQLException;
import io.rtdi.hanaappserver.utils.SessionHandler;
import io.rtdi.hanaappserver.utils.Util;

@Path("/importapp")
public class ImportService {
	protected final Logger log = LogManager.getLogger(this.getClass().getName());

	@Context
    private Configuration configuration;

	@Context 
	private ServletContext servletContext;
	
	@Context 
	private HttpServletRequest request;

	@GET
	@Path("{schema}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response runImport(@PathParam("schema") String schemaraw, @QueryParam("type") ImportType importtype) {
		HanaPrincipal user = (HanaPrincipal) request.getUserPrincipal();
		String username = user.getHanaUser();
		String rootpath = request.getServletContext().getRealPath("/protected/hanarepo");
		ArrayList<String> importresult = new ArrayList<>();
		try (Connection conn = SessionHandler.handleSession(request, log);) {
			username = Util.validateFilename(username);
			String schemaname = Util.decodeURIfull(schemaraw);
			schemaname = Util.validateFilename(schemaname);
			File rootdir = new File(rootpath + File.separatorChar + username  + File.separatorChar + schemaname);
			if (!rootdir.isDirectory()) {
				if (!rootdir.mkdirs()) {
					throw new IOException("Cannot create the director \"" + rootdir.getAbsolutePath() + "\" on the server");
				}
			}
			String sql = "select table_name from tables where schema_name = ? and is_user_defined_type = 'FALSE'";
			try (PreparedStatement stmt = conn.prepareStatement(sql);) {
				stmt.setString(1, schemaname);
				try (ResultSet rs = stmt.executeQuery();) {
					while (rs.next()) {
						String tablename = rs.getString(1);
						HDBTable hdbtable = Actions.createHDBTableFromDatabase(conn, schemaname, tablename);
						StringWriter w = new StringWriter();
						hdbtable.write(w);
						String path = Util.packageToPath(tablename);
						String filename = Util.packageToFilename(tablename);
						filename = filename + ".hdbtable";
						File d = new File(rootdir.getAbsolutePath() + File.separatorChar + path);
						if (!d.exists()) {
							if (!d.mkdirs()) {
								throw new HanaSQLException("Failed to create the directory", d.getAbsolutePath(), 10005);
							}
						}
						Files.writeString(new File(d.getAbsolutePath() + File.separatorChar + filename).toPath(), w.toString(), StandardOpenOption.CREATE);
					}
				}
			} catch (SQLException e) {
				throw new HanaSQLException(e, sql, "Please file an issue", 10002);
			}
			return Response.ok(importresult).build();
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage(e)).build();
		}
	}
	
}
