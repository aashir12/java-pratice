import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

class Book {
    String title;
    String author;
    int isbn;
    String genre;
    int copiesAvailable;
    LocalDate issueDate; // Added to track when a book is issued

    public Book(String title, String author, int isbn, String genre, int copiesAvailable) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.genre = genre;
        this.copiesAvailable = copiesAvailable;
    }

    public int getIsbn() {
        return isbn;
    }

    public int getCopiesAvailable() {
        return copiesAvailable;
    }

    public void setCopiesAvailable(int copiesAvailable) {
        this.copiesAvailable = copiesAvailable;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    @Override
    public String toString() {
        return "{ title: " + title + ", author: " + author + ", genre: " + genre +
                ", copiesAvailable: " + copiesAvailable + ", isbn: " + isbn +
                ", issueDate: " + issueDate + " }";
    }
}

class Member {
    String name;
    int memberId;
    List<Book> borrowedBooks;

    public Member(String name, int memberId) {
        this.name = name;
        this.memberId = memberId;
        this.borrowedBooks = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public int getMemberId() {
        return memberId;
    }

    public List<Book> getBorrowedBooks() {
        return borrowedBooks;
    }

    public void borrowBook(Book book) {
        borrowedBooks.add(book);
    }

    public void returnBook(Book book) {
        borrowedBooks.remove(book);
    }

    @Override
    public String toString() {
        return "{ name: " + name + ", memberId: " + memberId + ", borrowedBooks: " + borrowedBooks + " }";
    }
}

class Liberarian {
    private List<Book> books;
    private Map<Integer, Member> members; // Map of memberId to Member

    public Liberarian() {
        books = new ArrayList<>();
        members = new HashMap<>();
    }

    public void addBook(Book book) {
        books.add(book);
    }

    public boolean issueBook(int isbn, int memberId) {
        for (Book book : books) {
            if (book.getIsbn() == isbn && book.getCopiesAvailable() > 0) {
                Member member = members.get(memberId);
                if (member != null) {
                    book.setCopiesAvailable(book.getCopiesAvailable() - 1);
                    book.setIssueDate(LocalDate.now());
                    member.borrowBook(book);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean returnBook(int isbn, int memberId) {
        for (Book book : books) {
            if (book.getIsbn() == isbn) {
                Member member = members.get(memberId);
                if (member != null) {
                    book.setCopiesAvailable(book.getCopiesAvailable() + 1);
                    member.returnBook(book);
                    return true;
                }
            }
        }
        return false;
    }

    public void addMember(Member member) {
        members.put(member.getMemberId(), member);
    }

    public List<Book> getAvailableBooks() {
        return books;
    }

    public List<Book> getOverdueBooks() {
        List<Book> overdueBooks = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Book book : books) {
            if (book.getIssueDate() != null) {
                long daysIssued = ChronoUnit.DAYS.between(book.getIssueDate(), today);
                if (daysIssued > 7) { // Assume 7 days as the due period
                    overdueBooks.add(book);
                }
            }
        }
        return overdueBooks;
    }
}

class OverdueNotifier implements Runnable {
    private Liberarian librarian;

    public OverdueNotifier(Liberarian librarian) {
        this.librarian = librarian;
    }

    @Override
    public void run() {
        while (true) {
            List<Book> overdueBooks = librarian.getOverdueBooks();
            if (!overdueBooks.isEmpty()) {
                System.out.println("Overdue books found:");
                for (Book book : overdueBooks) {
                    System.out.println(book);
                }
            } else {
                System.out.println("No overdue books at the moment.");
            }
            try {
                Thread.sleep(5000); // Check every 5 seconds
            } catch (InterruptedException e) {
                System.out.println("Notifier interrupted!");
            }
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Liberarian librarian = new Liberarian();

        // Add books to the library
        Book book1 = new Book("Atomic Habits", "James Clear", 1, "Self-help", 5);
        Book book2 = new Book("The Alchemist", "Paulo Coelho", 2, "Fiction", 3);
        librarian.addBook(book1);
        librarian.addBook(book2);

        // Add members
        Member member1 = new Member("Alice", 101);
        Member member2 = new Member("Bob", 102);
        librarian.addMember(member1);
        librarian.addMember(member2);

        // Issue books
        librarian.issueBook(1, 101); // Member 101 borrows book with ISBN 1

        // Start overdue notification thread
        OverdueNotifier notifier = new OverdueNotifier(librarian);
        Thread notificationThread = new Thread(notifier);
        notificationThread.start();

        // Simulate delay and return
        try {
            Thread.sleep(10000); // Wait for 10 seconds
        } catch (InterruptedException e) {
            System.out.println("Main thread interrupted!");
        }

        // Member 101 returns the book
        librarian.returnBook(1, 101);
    }
}
