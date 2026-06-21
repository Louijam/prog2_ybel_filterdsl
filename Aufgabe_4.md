Visitor-Pattern (AstBuilderVisitor)
Vorteile:
Trennung von Struktur und Logik
Die AST-/Parse-Struktur bleibt sauber getrennt vom Code, der sie verarbeitet. Das ist klassisch OO und gut für größere Systeme.
Gut erweiterbar auf neue Operationen
Wenn du später z.B. noch
Pretty Printer
Evaluator
Optimizer
hinzufügen willst, kannst du einfach einen neuen Visitor schreiben, ohne die AST-Klassen zu ändern.
Skaliert gut bei vielen Operationen
Wenn du viele verschiedene “Verarbeitungen” auf demselben AST brauchst, ist Visitor ziemlich praktisch.

Nachteile:
Boilerplate / viel Overhead
viele visitXxx Methoden
Stack-Handling
viel indirekte Kontrolle
schwerer zu lesen
Die Logik ist verteilt über viele Methoden und nicht “linear”.
Zustand nötig
Hier brauchst du explizit Stacks (exprStack, valueStack), was fehleranfällig ist.
Debugging ist nervig
Du springst ständig zwischen Methoden statt einem klaren Ablauf.

Pattern Matching / rekursive Builder (AstBuilderPattern)
Vorteile:
sehr direkt und lesbar
Die Struktur entspricht fast 1:1 der Grammatik:

orExpr → andExpr (OR andExpr)*
kein Zustand nötig
Keine Stacks, keine Side-Effects → weniger Fehlerquellen.
funktionaler Stil
Jede Methode gibt direkt ein Ergebnis zurück → einfacher zu testen.
leichter zu debuggen
Du siehst direkt, was reinkommt und rausgeht.

Nachteile:
weniger flexibel bei vielen Operationen
Wenn du später z.B. Evaluator + Printer + Optimizer hast, musst du oft:
Logik duplizieren oder
neue rekursive Varianten bauen
enger gekoppelt an Struktur
Änderungen an der Grammatik führen oft zu Anpassungen in vielen Methoden.
nicht so gut für große AST-Frameworks
Bei komplexeren Compilern wird das schnell unübersichtlich.
