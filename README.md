# Computer-arithmetic

Здесь представлен код, который производит арифметику чисел с плавающей точкой в стандарте IEEE-754 с округлением к нулю в нескольких представлениях: single(f) и half(h) precision. На вход через командную строку через пробел подается тип представления, округление (0), а дальше либо одно число с плавающей точкой в шестнадцатеричной записи, либо два числа, а между ними знак операции. Вывод происходит не в стандартном виде, а в формате: < sign >-0x-<1 or 0>-< mantissa >-p-<exp (with sign)>.

Работаю над:
1. Добавлением Фиксированной точки
2. Добавлением других типов округления
3. Добавление других представлений чисел (double precision, bfloat precision и т.д.)
