package profuturo.nci.cifrascontrol.sf;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbUserException;
import com.ibm.broker.plugin.MbXMLNSC;
import com.ibm.broker.plugin.MbNode.JDBC_TransactionType;

public class ConsultarRegistrosCreditoVivienda_qry_registrosCreditoVivienda
		extends MbJavaComputeNode {

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
			MbElement outRegistrosCreditoViviendaResponse = outBody.createElementAsFirstChild(MbXMLNSC.FOLDER, "registrosCreditoViviendaResponse", null);

			String qry = "SELECT FTN_ID_VIV_GARANTIA, COUNT(*) NUM_REGISTROS " +
					"FROM CIERREN_ETL.TLSISGRAL_ETL_VAL_CLIENTE " +
					"WHERE FTC_FOLIO = ? " +
					"AND FTC_ID_ARCHIVO = ? " +
					"GROUP BY FTN_ID_VIV_GARANTIA";

			conn = getJDBCType4Connection("DS_CIERREN_ETL", JDBC_TransactionType.MB_TRANSACTION_AUTO);

			pstmt = conn.prepareStatement(qry);
			pstmt.setString(1, (String) inRoot.evaluateXPath("string(/registrosCreditoViviendaRequest/folio)"));
			pstmt.setString(2, (String) inRoot.evaluateXPath("string(/registrosCreditoViviendaRequest/idArchivo)"));

			rs = pstmt.executeQuery();

			while (rs.next()) {
				MbElement registroCreditoVivienda = outRegistrosCreditoViviendaResponse.createElementAsLastChild(MbXMLNSC.FOLDER, "registroCreditoVivienda", null);

				registroCreditoVivienda.createElementAsLastChild(MbXMLNSC.FIELD, "creditoVivienda", rs.getBigDecimal("FTN_ID_VIV_GARANTIA"));
				registroCreditoVivienda.createElementAsLastChild(MbXMLNSC.FIELD, "numeroRegistros", rs.getBigDecimal("NUM_REGISTROS"));
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
			} catch (Exception e) {
				rs = null;
				pstmt = null;
				conn = null;
				throw new MbUserException(this, "finally", "", "", e.toString(), null);
			}
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
