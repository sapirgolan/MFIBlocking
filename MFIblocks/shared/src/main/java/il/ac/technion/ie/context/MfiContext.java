package il.ac.technion.ie.context;


import il.ac.technion.ie.types.Alg;
import il.ac.technion.ie.types.MFISetsCheckConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;


public class MfiContext {
	
	private double[] minBlockingThresholds;
	private int[] minSup;
	private double[] neighborhoodGrowth;
	private MFISetsCheckConfiguration configuration;
	private String matchFile;
	private Alg alg;
	private String lexiconFile;
	private String recordsFile;
    private String origRecordsFileWithoutCommas;
    private boolean inPerformanceMode;
	private int firstDbSize;
	private String printFormat;
	private String originalRecordsPath;
    private String datasetName;

    public MfiContext() {
        this.firstDbSize = 0;
        this.configuration = MFISetsCheckConfiguration.DEFAULT;
    }

    public void setAlgorithm(Alg alg) {
        this.alg = alg;
    }

    public void setNGs(String neighborhoodGrowth) {
        this.neighborhoodGrowth = getDoubles(neighborhoodGrowth);
    }

    public void setOrigRecordsFileWithoutCommas(String origRecordsFileWithoutCommas) {
        this.origRecordsFileWithoutCommas = origRecordsFileWithoutCommas;
    }

    public void setPerformanceFlag(String[] args) {
        this.inPerformanceMode = false;
        int lastArgument = args.length - 1;
        if ("perf".equalsIgnoreCase(args[lastArgument])) {
            this.inPerformanceMode = true;
        }
    }

    public String getLexiconFile() {
        return lexiconFile;
    }

	public void setLexiconFile(String lexiconFile) {
		this.lexiconFile = lexiconFile;
	}

    public String getRecordsFile() {
        return recordsFile;
    }

	public void setRecordsFile(String recordsFile) {
		this.recordsFile = recordsFile;
	}

    public String getOriginalFile() {
        return origRecordsFileWithoutCommas;
    }

    private double[] getThresholds(String strDoubles) {
        String[] thresholds = strDoubles.split(",");
        double[] dThresholds = new double[thresholds.length];
        for (int i = 0; i < thresholds.length; i++) {
            dThresholds[i] = Double.parseDouble(thresholds[i].trim());
        }
        return dThresholds;
    }

    private int[] getInts(String strInts) {
        String[] intStrs = strInts.trim().split(",");
        int[] ints = new int[intStrs.length];
        for (int i = 0; i < intStrs.length; i++) {
            ints[i] = Integer.parseInt(intStrs[i].trim());
        }
        return ints;
    }

    private double[] getDoubles(String strDbs) {
        String[] dbStrs = strDbs.trim().split(",");
        double[] dbs = new double[dbStrs.length];
        for (int i = 0; i < dbStrs.length; i++) {
            dbs[i] = Double.parseDouble(dbStrs[i].trim());
        }
        return dbs;
    }

    public double[] getMinBlockingThresholds() {
        return this.minBlockingThresholds;
    }

    public void setMinBlockingThresholds(String minBlockingThresholds) {
        this.minBlockingThresholds = getThresholds(minBlockingThresholds);

	}

    public String getAlgName() {
        return alg.toString();
    }

    public double[] getNeighborhoodGrowth() {
        return neighborhoodGrowth;
    }

    public int[] getMinSup() {
        return this.minSup;
    }

    public void setMinSup(String minSup) {
        int[] minSupInt = getInts(minSup);
        Arrays.sort(minSupInt);
        this.minSup = minSupInt;
    }

    public String getMatchFile() {
        return this.matchFile;
    }

    public void setMatchFile(String matchFile) {
        this.matchFile = matchFile;
    }

	public MFISetsCheckConfiguration getConfig() {
		return this.configuration;
	}

	public boolean isInPerformanceMode() {
		return this.inPerformanceMode;
    }

    public int getFirstDbSize() {
        return firstDbSize;
    }

	public void setFirstDbSize(String[] args) {
		if(args.length > 10 && args[10] != null){
			if (StringUtils.isNumeric(args[10]) ) {
				this.firstDbSize = Integer.parseInt(args[10]);
			}
		}
	}

	public void setConfiguration(String configuration) {
		try {
			this.configuration = MFISetsCheckConfiguration.valueOf(configuration);
		} catch (Exception e) {
			System.err.println(String.format("Failed to read value of configuration, will use %s instead", MFISetsCheckConfiguration.DEFAULT.toString()));
		}
	}

	public void setPrintFormat(String string) {
		String[] input = string.trim().split(",");
		if (input[0]==null){
			System.err.println("The chosen format for block printing is unsupported.");
		}
		else {
			if (input[0].equalsIgnoreCase("B") || input[0].equalsIgnoreCase("N") || input[0].equalsIgnoreCase("S"))
				printFormat=input[0];
            else
                System.err.println("The chosen format for block printing is unsupported.");
			if (input.length>1){
			originalRecordsPath=input[1];
			}
		}
	}

	public String getPrntFormat() {
		return printFormat;
	}

	public String getOriginalRecordsPath() {
		return originalRecordsPath;
	}

    public void setOriginalRecordsPath(String recordsPath) {
        this.originalRecordsPath = recordsPath;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }
}
