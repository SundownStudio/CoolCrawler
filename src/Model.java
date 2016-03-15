import java.util.HashSet;
import java.util.Set;

public class Model {

	/* holds email addresses */
	private Set<String> emails;

	/* so we don't visit same url */
	private Set<String> history;

	/* list of destinations */
	private String[] destinations;

	/* iterating through list of destinations */
	private int index;

	public Model(int maxEmails, int maxURLs) {
		this.emails = new HashSet<>(maxEmails);
		this.destinations = new String[maxURLs];
		this.history = new HashSet<>();
	}

	public boolean hasMoreURLsToVisit() { return (index < history.size()); }

	public int getNumEmails() { return emails.size(); }

	public String getNextDestination() { return destinations[index++]; }

	public int getHistorySize() { return history.size(); }

	public boolean addEmail(String email) { return emails.add(email); }

	public boolean addURL(String url) {
		int numUrls = history.size();
		if (history.add(url)) {
			destinations[numUrls] = url;
			return true;
		}
		return false;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("\n\nFound " + emails.size() + " emails in first " + history.size() + " urls\n");
		for (String s : emails)
			sb.append(s + "\n");
		return sb.toString();
	}

	public void clear() {
		history.clear();
		emails.clear();
		index = 0;
	}
}
