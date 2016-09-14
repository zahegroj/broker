package profuturo.nci.movimientos.sf;

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

public class ConsultarMovPreviosRelacionados_qry_MovPreviosRelacionados extends
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
			MbElement outMovPreviosRelacionadosResponse = outBody.createElementAsFirstChild(MbXMLNSC.FOLDER, "movPreviosRelacionadosResponse", null);

			String qry = "SELECT FTC_FOLIO, FTC_FOLIO_REL, SUM(FTF_MONTO_PESOS) FTF_MONTO_PESOS, SUM(FTF_MONTO_ACCIONES) FTF_MONTO_ACCIONES, COUNT(*) NUM_REGISTROS " +
					"FROM CIERREN_ETL.TTSISGRAL_ETL_MOVIMIENTOS " +
					"WHERE FTC_FOLIO = ? " +
					"AND FTC_FOLIO_REL IS NOT NULL " +
					"GROUP BY FTC_FOLIO, FTC_FOLIO_REL " +
					"ORDER BY FTC_FOLIO_REL";

			conn = getJDBCType4Connection("DS_CIERREN_ETL", JDBC_TransactionType.MB_TRANSACTION_AUTO);

			pstmt = conn.prepareStatement(qry);
			pstmt.setString(1, (String) inRoot.evaluateXPath("string(/consultarMovPreviosRelacionados/folio)"));

			rs = pstmt.executeQuery();

			while (rs.next()) {
				MbElement movPreviosRelacionados = outMovPreviosRelacionadosResponse.createElementAsLastChild(MbXMLNSC.FOLDER, "movPreviosRelacionados", null);

				movPreviosRelacionados.createElementAsLastChild(MbXMLNSC.FIELD, "folio", rs.getBigDecimal("FTC_FOLIO"));
				movPreviosRelacionados.createElementAsLastChild(MbXMLNSC.FIELD, "folioRel", rs.getBigDecimal("FTC_FOLIO_REL"));
				movPreviosRelacionados.createElementAsLastChild(MbXMLNSC.FIELD, "montoPesos", rs.getBigDecimal("FTF_MONTO_PESOS"));
				movPreviosRelacionados.createElementAsLastChild(MbXMLNSC.FIELD, "montoAcciones", rs.getBigDecimal("FTF_MONTO_ACCIONES"));
				movPreviosRelacionados.createElementAsLastChild(MbXMLNSC.FIELD, "numeroRegistros", rs.getLong("NUM_REGISTROS"));
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
