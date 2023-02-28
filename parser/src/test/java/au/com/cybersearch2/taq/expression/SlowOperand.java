package au.com.cybersearch2.taq.expression;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.j256.simplelogging.Logger;

import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.log.LogManager;
import au.com.cybersearch2.taq.operator.AssignOnlyOperator;
import au.com.cybersearch2.taq.service.LoopMonitor;

public class SlowOperand extends Operand {

    static final String CREATE_SQL = "create table models ( _id integer primary key autoincrement, name text, description text);\n";
    static final String INSERT_SQL = "INSERT INTO models (name, description) VALUES (?,?)";

    private final static Logger logger = LogManager.getLogger(SlowOperand.class);

    private static int idSource;
    
	private final Operator operator;
	private int dimension;
	private int phase;
	private int identity;
	
	public SlowOperand(String name, int dimension) {
		super(name);
		this.dimension = dimension;
		identity = ++idSource;
		operator = new AssignOnlyOperator();
	}

	@Override
	public EvaluationStatus evaluate(int id) {
	    LoopMonitor monitor = context.getLoopMonitor();
        String fname = System.getProperty("user.home");
        fname = Paths.get(fname, ".taq", "test" + identity) 
                .toAbsolutePath().normalize().toString();
        File testDir = new File(fname);
        boolean ok = testDir.exists();
        if (!ok)
        	testDir.mkdirs();
        File dbFile = new File(testDir,"tortoise.db");
        if (ok && (phase == 0) && dbFile.exists())	
        	dbFile.delete();
        ok = true;
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath())) {
        	if (phase == 0) {
    		    begin(connection);
    		    try (Statement stmt = connection.createStatement()) {
    			    stmt.execute(CREATE_SQL);
    	    		commit(connection);
    		    }
    		}
    		int start = (phase * dimension) + 1;
    		for (int i = start; i <= start + dimension; ++i) {
    			writeRecord(connection, i);
    			if (monitor.isInterrupted()) {
			    	logger.error(String.format("Loop monitor terminated SlowOperand %s", getName()));
			    	ok = false;
			    	break;
    			}
    		}
    		++phase;
        } catch (SQLException e) {
			e.printStackTrace();
			ok = false;
		}
        //if (!ok)
		//	return EvaluationStatus.FAIL;
        EvaluationStatus status = EvaluationStatus.COMPLETE;
        if (phase == dimension) {
        	phase = 0;
        	status = EvaluationStatus.SHORT_CIRCUIT;
        }
		//System.out.println("Tortoise" + identity + " Dim = " + dimension + " Phase = " + phase);
		return  status;
	}

	@Override
	public void assign(Parameter parameter) {
	}

	@Override
	public QualifiedName getQualifiedName() {
		return QualifiedName.ANONYMOUS;
	}

	@Override
	public Operand getRightOperand() {
		return null;
	}

	@Override
	public Operator getOperator() {
		return operator;
	}

	private void begin(Connection connection) throws SQLException {
		String sql = "BEGIN";
		try (Statement stmt = connection.createStatement()) {
			stmt.execute(sql);
		}
	}

	private void writeRecord(Connection connection, int iteration) throws SQLException {
		begin(connection);
		try (PreparedStatement pstmt = connection.prepareStatement(INSERT_SQL)) {
			pstmt.setString(1, "name" + iteration);
			pstmt.setString(2, "description" + iteration);
			pstmt.executeUpdate();
		}
		commit(connection);
	}
	
	private void commit(Connection connection) throws SQLException {
		String sql = "COMMIT";
		try (Statement stmt = connection.createStatement()) {
			stmt.execute(sql);
		}
	}
}
