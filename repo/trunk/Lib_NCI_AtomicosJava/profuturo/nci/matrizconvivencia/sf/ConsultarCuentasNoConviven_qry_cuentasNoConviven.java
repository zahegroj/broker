package profuturo.nci.matrizconvivencia.sf;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbTimestamp;
import com.ibm.broker.plugin.MbUserException;
import com.ibm.broker.plugin.MbXMLNSC;
import com.ibm.broker.plugin.MbNode.JDBC_TransactionType;

public class ConsultarCuentasNoConviven_qry_cuentasNoConviven extends
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
			
			String qry = "SELECT MAT.FTN_NUM_CTA_INVDUAL, " +
						 " CLI.FTN_NSS, " +
						 " CLI.FTC_CURP, " +
						 " RTRIM(CLI.FTC_NOMBRE) FTC_NOMBRE, " +
						 " MAT.FTB_ESTATUS_MARCA, " +
                    	 " CASE WHEN MAT.FTB_ESTATUS_MARCA = 0 THEN 'NO CONVIVE' ELSE 'CONVIVIE' END DESC_ESTATUS, " +
                         " FTN_ID_ERROR_VAL, " +
                         " FTN_ID_SUBPROC_NO_CONV, " +
                         " MAT.FTD_FECHA, " +
                         " MAT.FCN_ID_TIPO_SUBCTA " +
                         " FROM CIERREN_ETL.TLSISGRAL_ETL_VAL_MATRIZ_CONV MAT, CIERREN_ETL.TLSISGRAL_ETL_VAL_CLIENTE CLI " +
						 " WHERE MAT.FTC_FOLIO         = ? " +
						 " AND MAT.FTC_FOLIO           = CLI.FTC_FOLIO " +
						 " AND MAT.FTN_NUM_CTA_INVDUAL = CLI.FTC_NUM_CUENTA " +
						 " AND MAT.FTB_ESTATUS_MARCA   = 0 " +
						 " AND ROWNUM < 101";
			
			conn = getJDBCType4Connection("DS_CIERREN_ETL", JDBC_TransactionType.MB_TRANSACTION_AUTO);
			
			pstmt = conn.prepareStatement(qry);
			pstmt.setString(1, (String) inRoot.evaluateXPath("string(/consultarCuentasNoConviven/folio)"));
			
			rs = pstmt.executeQuery();
			while (rs.next()) {
				MbElement cuentaNoConvive = res.createElementAsFirstChild(MbXMLNSC.FOLDER, "cuentaNoConvive", null);

				cuentaNoConvive.createElementAsLastChild(MbXMLNSC.FIELD, "numeroCuenta", rs.getString("FTN_NUM_CTA_INVDUAL"));
				cuentaNoConvive.createElementAsLastChild(MbXMLNSC.FIELD, "nss", rs.getString("FTN_NSS"));
				cuentaNoConvive.createElementAsLastChild(MbXMLNSC.FIELD, "curp", rs.getString("FTC_CURP"));
				cuentaNoConvive.createElementAsLastChild(MbXMLNSC.FIELD, "nombreTrabajador", rs.getString("FTC_NOMBRE"));
				cuentaNoConvive.createElementAsLastChild(MbXMLNSC.FIELD, "estatusConvivencia", rs.getString("FTB_ESTATUS_MARCA"));
				cuentaNoConvive.createElementAsLastChild(MbXMLNSC.FIELD, "descEstatusConvivencia", rs.getString("DESC_ESTATUS"));
				cuentaNoConvive.createElementAsLastChild(MbXMLNSC.FIELD, "idErrorNoConvive", rs.getString("FTN_ID_ERROR_VAL"));
				cuentaNoConvive.createElementAsLastChild(MbXMLNSC.FIELD, "idSubrocesoNoConvive", rs.getString("FTN_ID_SUBPROC_NO_CONV"));
				
				MbTimestamp mbTimestamp = null;				
				Timestamp timestamp = rs.getTimestamp("FTD_FECHA");
				if (timestamp != null) {
					Date fechaRegistro = new java.util.Date(timestamp.getTime());
					Calendar cal = Calendar.getInstance();
					cal.setTime(fechaRegistro);
					mbTimestamp = new MbTimestamp(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE),
							cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), cal.get(Calendar.MILLISECOND));
				}
				
				cuentaNoConvive.createElementAsLastChild(MbXMLNSC.FIELD, "fechaOcurrencia", mbTimestamp);
				cuentaNoConvive.createElementAsLastChild(MbXMLNSC.FIELD, "idTipoSubcuenta", rs.getString("FCN_ID_TIPO_SUBCTA"));
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
