package profuturo.nci.cliente.sf;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbDate;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbTimestamp;
import com.ibm.broker.plugin.MbUserException;
import com.ibm.broker.plugin.MbXMLNSC;
import com.ibm.broker.plugin.MbNode.JDBC_TransactionType;

public class ConsultarClientesNoIdentificados_qry_ClientesNoIdent extends
		MbJavaComputeNode {

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		MbOutputTerminal alt = getOutputTerminal("alternate");

		MbMessage inMessage = inAssembly.getMessage();

		// create new empty message
		MbMessage outMessage = new MbMessage();
		MbMessageAssembly outAssembly = new MbMessageAssembly(inAssembly,
				outMessage);
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			// optionally copy message headers
			copyMessageHeaders(inMessage, outMessage);
			// ----------------------------------------------------------
			// Add user code below
			MbElement inRoot = inMessage.getRootElement();
			MbElement outRoot = outMessage.getRootElement();
			MbElement outBody = outRoot.createElementAsLastChild("XMLNSC");
			MbElement res = outBody.createElementAsFirstChild(MbXMLNSC.FOLDER, "res", null);
			
			String qry = "SELECT T.FTN_NSS, RTRIM(T.FTC_NOMBRE) FTC_NOMBRE, RTRIM(T.FTC_CURP) FTC_CURP, T.FTC_IDENTIFICADOS, T.FTC_CLAVE_ENT_RECEP, " +
					"T.FTN_ID_DIAGNOSTICO, T.FTN_ID_SUBP_NO_CONV, T.FTD_FECHA_CERTIFICACION, NVL(TO_CHAR(T.FTC_NUM_CUENTA),'-') FTC_NUM_CUENTA " +
				" FROM TLSISGRAL_ETL_VAL_CLIENTE T" +
				" WHERE T.FTC_FOLIO = ?" +
				" AND T.FTC_ID_ARCHIVO = ?" +
				" AND T.FTC_IDENTIFICADOS = '0'" +
				" AND ROWNUM < 101";
			
			conn = getJDBCType4Connection("DS_CIERREN_ETL", JDBC_TransactionType.MB_TRANSACTION_AUTO);

			pstmt = conn.prepareStatement(qry);
			pstmt.setString(1, (String) inRoot.evaluateXPath("string(/folio)"));			
			pstmt.setString(2, (String) inRoot.evaluateXPath("string(/idarchivo)"));
			
			rs = pstmt.executeQuery();
			while (rs.next()) {
				MbElement consultaClientesNI = res.createElementAsFirstChild(MbXMLNSC.FOLDER, "consultaClientesNI", null);

				consultaClientesNI.createElementAsLastChild(MbXMLNSC.FIELD, "nss", rs.getString("FTN_NSS"));
				consultaClientesNI.createElementAsLastChild(MbXMLNSC.FIELD, "nombre", rs.getString("FTC_NOMBRE"));
				consultaClientesNI.createElementAsLastChild(MbXMLNSC.FIELD, "curp", rs.getString("FTC_CURP"));
				consultaClientesNI.createElementAsLastChild(MbXMLNSC.FIELD, "estatus", rs.getString("FTC_IDENTIFICADOS"));
				consultaClientesNI.createElementAsLastChild(MbXMLNSC.FIELD, "claveEntRecep", rs.getString("FTC_CLAVE_ENT_RECEP"));
				consultaClientesNI.createElementAsLastChild(MbXMLNSC.FIELD, "idDiagnostico", rs.getString("FTN_ID_DIAGNOSTICO"));
				consultaClientesNI.createElementAsLastChild(MbXMLNSC.FIELD, "idSubprocesoNoIdent", rs.getString("FTN_ID_SUBP_NO_CONV"));
				
				MbTimestamp mbTimestamp = null;				
				java.sql.Date dateCertificacion = rs.getDate("FTD_FECHA_CERTIFICACION");
				if (dateCertificacion != null) {
					Date fechaCertificacion = new java.util.Date(dateCertificacion.getTime());
					Calendar cal = Calendar.getInstance();
					cal.setTime(fechaCertificacion);
					mbTimestamp = new MbTimestamp(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE),
							cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), cal.get(Calendar.MILLISECOND));
				}
				
				consultaClientesNI.createElementAsLastChild(MbXMLNSC.FIELD, "fechaCertificacion", mbTimestamp);
				consultaClientesNI.createElementAsLastChild(MbXMLNSC.FIELD, "numeroCuenta", rs.getString("FTC_NUM_CUENTA"));
			}
			
			// End of user code
			// ----------------------------------------------------------
		} catch (MbException e) {
			// Re-throw to allow Broker handling of MbException
			throw e;
		} catch (RuntimeException e) {
			// Re-throw to allow Broker handling of RuntimeException
			throw e;
		} catch (Exception e) {
			// Consider replacing Exception with type(s) thrown by user code
			// Example handling ensures all exceptions are re-thrown to be handled in the flow
			throw new MbUserException(this, "evaluate()", "", "", e.toString(),
					null);
		} finally {
			try {
				rs.close();
				pstmt.close();
				conn.close();
			}catch(Exception e) {rs = null; pstmt = null; conn = null;
				throw new MbUserException(this, "finally", "", "", e.toString(),
					null);};
		}
		// The following should only be changed
		// if not propagating message to the 'out' terminal
		out.propagate(outAssembly);
	}

	public void copyMessageHeaders(MbMessage inMessage, MbMessage outMessage)
			throws MbException {
		MbElement outRoot = outMessage.getRootElement();

		// iterate though the headers starting with the first child of the root
		// element
		MbElement header = inMessage.getRootElement().getFirstChild();
		while (header != null && header.getNextSibling() != null) // stop before
																	// the last
																	// child
																	// (body)
		{
			// copy the header and add it to the out message
			outRoot.addAsLastChild(header.copy());
			// move along to next header
			header = header.getNextSibling();
		}
	}

}
