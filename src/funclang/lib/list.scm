
/* Additional list procedures with semantics similar to Scheme lists */

(define cadr
	(lambda (lst)
		(car (cdr lst))
	)
)

(define caddr
	(lambda (lst)
		(car (cdr (cdr lst)))
	)
)

(define length
	(lambda (lst)
		(if (null? lst) 0
			(+ 1 (length (cdr lst)))
		)		
	)
)

(define append
	(lambda (lst1 lst2)
		(if (null? lst1) lst2
			(if (null? lst2) lst1
				(cons (car lst1) (append (cdr lst1) lst2))
			)
		)		
	)
)

(define map
	(lambda (op lst)
		(if (null? lst) (list)
			(cons (op (car lst)) (map op (cdr lst)))
		)		
	)
)