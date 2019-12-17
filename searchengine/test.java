package searchengine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Posting p = new Posting(1, new Document(1, "dgwdg", 1), "d");
		Posting p1 = new Posting(1, new Document(1, "sdggsd", 1), "d");

		Posting p2 = new Posting(1, new Document(1, "sdggsd", 2), "d");
		System.out.println(p.equals(p2));

		List<Posting> list1 = new ArrayList<Posting>(Arrays.asList(p));
		List<Posting> list2 = new ArrayList<Posting>(Arrays.asList(p1));

		list1.retainAll(list2);

		System.out.println(list1);

	}

}
