package profuturo.nci.cifrascontrol.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbTimestamp;
import com.ibm.broker.plugin.MbUserException;
import com.ibm.broker.plugin.MbXMLNSC;

public class ConsultarCifrasControl_qry_CifrasControl extends MbJavaComputeNode {

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		MbOutputTerminal alt = getOutputTerminal("alternate");

		MbMessage inMessage = inAssembly.getMessage();

		// create new empty message
		MbMessage outMessage = new MbMessage();
		MbMessageAssembly outAssembly = new MbMessageAssembly(inAssembly, outMessage);

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

			String qry = "SELECT T.*" +
				" FROM TLSISGRAL_ETL_VAL_CIFRAS_CTRL T" +
				" WHERE T.FTC_FOLIO = ? " +
				" AND T.FTC_ID_SUBETAPA = ? " +
				" ORDER BY T.FLD_FEC_REG DESC";
			
			conn = getJDBCType4Connection("DS_CIERREN_ETL", JDBC_TransactionType.MB_TRANSACTION_AUTO);
			
			pstmt = conn.prepareStatement(qry);
			pstmt.setString(1, (String) inRoot.evaluateXPath("string(/consultarCifrasControl/folio)"));
			String idSubetapa = (String) inRoot.evaluateXPath("string(/consultarCifrasControl/idSubetapa)");
			pstmt.setInt(2, Integer.parseInt(idSubetapa));
			
			rs = pstmt.executeQuery();
			while (rs.next()) {
				MbElement outCifrasControlResultSet = outBody.createElementAsFirstChild(MbXMLNSC.FOLDER, "cifrasControlResultSet", null);

				outCifrasControlResultSet.createElementAsLastChild(MbXMLNSC.FIELD, "folio", rs.getString("FTC_FOLIO"));
				outCifrasControlResultSet.createElementAsLastChild(MbXMLNSC.FIELD, "idSubetapa", rs.getInt("FTC_ID_SUBETAPA"));
				outCifrasControlResultSet.createElementAsLastChild(MbXMLNSC.FIELD, "totalRegistros", rs.getInt("FLN_TOTAL_REGISTROS"));
				outCifrasControlResultSet.createElementAsLastChild(MbXMLNSC.FIELD, "registrosCumplieron", rs.getInt("FLN_REG_CUMPLIERON"));
				outCifrasControlResultSet.createElementAsLastChild(MbXMLNSC.FIELD, "registrosNoCumplieron", rs.getInt("FLN_REG_NO_CUMPLIERON"));
				outCifrasControlResultSet.createElementAsLastChild(MbXMLNSC.FIELD, "validacion", rs.getString("FLC_VALIDACION"));
				outCifrasControlResultSet.createElementAsLastChild(MbXMLNSC.FIELD, "totalErrores", rs.getInt("FLN_TOTAL_ERRORES"));
				outCifrasControlResultSet.createElementAsLastChild(MbXMLNSC.FIELD, "detalle", rs.getString("FLC_DETALLE"));
				
				MbTimestamp mbTimestamp = null;				
				Timestamp timestamp = rs.getTimestamp("FLD_FEC_REG");
				if (timestamp != null) {
					Date fechaRegistro = new java.util.Date(timestamp.getTime());
					Calendar cal = Calendar.getInstance();
					cal.setTime(fechaRegistro);
					mbTimestamp = new MbTimestamp(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE),
							cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), cal.get(Calendar.MILLISECOND));
				}
				outCifrasControlResultSet.createElementAsLastChild(MbXMLNSC.FIELD, "fechaRegistro", mbTimestamp);
				outCifrasControlResultSet.createElementAsLastChild(MbXMLNSC.FIELD, "usuario", rs.getString("FLC_USU_REG"));
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
