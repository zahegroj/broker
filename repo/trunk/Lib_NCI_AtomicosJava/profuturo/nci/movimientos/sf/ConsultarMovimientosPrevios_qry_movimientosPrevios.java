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

public class ConsultarMovimientosPrevios_qry_movimientosPrevios extends
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
		PreparedStatement pstmt2 = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		try {
			// optionally copy message headers
			copyMessageHeaders(inMessage, outMessage);
			// ----------------------------------------------------------
			// Add user code below
			MbElement inRoot = inMessage.getRootElement();
			MbElement outRoot = outMessage.getRootElement();
			MbElement outBody = outRoot.createElementAsLastChild("XMLNSC");
			MbElement outMovimientosPreviosResponse = outBody.createElementAsFirstChild(MbXMLNSC.FOLDER, "movimientosPreviosResponse", null);

			String qry = "SELECT FCN_ID_SIEFORE, " +
						    "FCN_ID_TIPO_SUBCTA, " +
						    "FCN_ID_TIPO_MOV, " +
						    "SUM(FTF_MONTO_PESOS) FTF_MONTO_PESOS, " +
						    "SUM(FTF_MONTO_ACCIONES) FTF_MONTO_ACCIONES, " +
						    "COUNT(FTC_FOLIO) NUM_REGISTROS " +
						"FROM CIERREN_ETL.TTSISGRAL_ETL_MOVIMIENTOS " +
						"WHERE FTC_FOLIO = ? " +
						"GROUP BY FCN_ID_SIEFORE, " +
						    "FCN_ID_TIPO_SUBCTA, " +
						    "FCN_ID_TIPO_MOV " +
						"ORDER BY FCN_ID_SIEFORE, " +
						    "FCN_ID_TIPO_SUBCTA, " +
						    "FCN_ID_TIPO_MOV ";
			
			conn = getJDBCType4Connection("DS_CIERREN_ETL", JDBC_TransactionType.MB_TRANSACTION_AUTO);
			
			pstmt = conn.prepareStatement(qry);
			pstmt.setString(1, (String) inRoot.evaluateXPath("string(/consultarMovimientosPrevios/folio)"));
			
			rs = pstmt.executeQuery();
			int idSieforePrevia = -1;
			MbElement outMovimientosPrevios = null;
			
			while (rs.next()) {
				int idSiefore = rs.getInt("FCN_ID_SIEFORE");
				
				if (idSiefore != idSieforePrevia) {
					outMovimientosPrevios = outMovimientosPreviosResponse.createElementAsFirstChild(MbXMLNSC.FOLDER, "movimientosPrevios", null);
					outMovimientosPrevios.createElementAsLastChild(MbXMLNSC.FIELD, "idSiefore", idSiefore);
				}
				
				MbElement outMovimientosSubcuentas = outMovimientosPrevios.createElementAsFirstChild(MbXMLNSC.FOLDER, "subcuentas", null);
				
				outMovimientosSubcuentas.createElementAsLastChild(MbXMLNSC.FIELD, "idTipoSubcuenta", rs.getInt("FCN_ID_TIPO_SUBCTA"));
				outMovimientosSubcuentas.createElementAsLastChild(MbXMLNSC.FIELD, "idTipoMovimiento", rs.getInt("FCN_ID_TIPO_MOV"));
				outMovimientosSubcuentas.createElementAsLastChild(MbXMLNSC.FIELD, "montoPesos", rs.getBigDecimal("FTF_MONTO_PESOS"));
				outMovimientosSubcuentas.createElementAsLastChild(MbXMLNSC.FIELD, "montoAcciones", rs.getBigDecimal("FTF_MONTO_ACCIONES"));
				outMovimientosSubcuentas.createElementAsLastChild(MbXMLNSC.FIELD, "numeroRegistros", rs.getLong("NUM_REGISTROS"));
				
				idSieforePrevia = idSiefore;
			}
			
			String qryTotal = "SELECT FCN_ID_SIEFORE, " +
								"NVL(SUM(FTF_MONTO_PESOS), 0) FTF_MONTO_PESOS, " +
								"NVL(SUM(FTF_MONTO_ACCIONES), 0) FTF_MONTO_ACCIONES, " +
								"COUNT(FTC_FOLIO) NUM_REGISTROS " +
								"FROM CIERREN_ETL.TTSISGRAL_ETL_MOVIMIENTOS " +
								"WHERE FTC_FOLIO = ? " +
								"GROUP BY FCN_ID_SIEFORE";
			
			pstmt2 = conn.prepareStatement(qryTotal);
			pstmt2.setString(1, (String) inRoot.evaluateXPath("string(/consultarMovimientosPrevios/folio)"));
			
			rs2 = pstmt2.executeQuery();
			while (rs2.next()) {
				MbElement movimientosPreviosTotal = outMovimientosPreviosResponse.createElementAsFirstChild(MbXMLNSC.FOLDER, "movimientosPreviosTotal", null);

				movimientosPreviosTotal.createElementAsLastChild(MbXMLNSC.FIELD, "idSiefore", rs2.getInt("FCN_ID_SIEFORE"));
				movimientosPreviosTotal.createElementAsLastChild(MbXMLNSC.FIELD, "montoPesos", rs2.getBigDecimal("FTF_MONTO_PESOS"));
				movimientosPreviosTotal.createElementAsLastChild(MbXMLNSC.FIELD, "montoAcciones", rs2.getBigDecimal("FTF_MONTO_ACCIONES"));
				movimientosPreviosTotal.createElementAsLastChild(MbXMLNSC.FIELD, "numeroRegistros", rs2.getLong("NUM_REGISTROS"));
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
				rs2.close();
				pstmt2.close();
				conn.close();
			}catch(Exception e) {rs = null; rs2 = null; pstmt = null; pstmt2 = null; conn = null;
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
