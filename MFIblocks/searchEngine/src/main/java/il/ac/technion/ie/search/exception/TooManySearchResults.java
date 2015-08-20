package il.ac.technion.ie.search.exception;

public class TooManySearchResults extends Exception {

	private static final long serialVersionUID = 7943037188616177167L;
	
	public TooManySearchResults(String Message) {
		super(Message);
	}
}
