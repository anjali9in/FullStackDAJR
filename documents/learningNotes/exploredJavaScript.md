# Explored JavaScript

Revision notes for a 1-5 years JavaScript developer interview, with extra focus on concepts that matter in React interviews.

JavaScript is a high-level, dynamically typed, prototype-based, multi-paradigm language. It runs in browsers, Node.js, runtimes like Deno/Bun, and embedded environments. For frontend interviews, JavaScript fundamentals are more important than memorizing syntax because React, Redux, routing, async APIs, build tools, and browser behavior all depend on these basics.

Current version context: ECMAScript 2025 is the latest published yearly snapshot in the official spec. TC39 finished proposals already list expected ECMAScript 2026 features such as `Array.fromAsync`, `Error.isError`, `Uint8Array` Base64 helpers, `Math.sumPrecise`, iterator sequencing, JSON parse source text access, and upsert APIs.

---

# 1. JavaScript In One Paragraph

JavaScript executes code on a single main thread using a call stack, heap, event loop, task queue, and microtask queue. It supports first-class functions, closures, lexical scope, prototypes, classes, promises, modules, async/await, and dynamic objects. In frontend apps, it manipulates data, handles browser events, calls APIs, updates UI state, and coordinates async work.

Interview answer:

```text
JavaScript is a single-threaded, event-driven language with asynchronous capabilities through callbacks, promises, async/await, and the event loop. It is dynamically typed, uses lexical scoping and closures, and objects inherit through prototypes. Modern JavaScript uses modules, classes, destructuring, spread/rest, optional chaining, nullish coalescing, and promise utilities.
```

---

# 2. ECMAScript vs JavaScript

## ECMAScript

ECMAScript is the language specification standardized by Ecma International and TC39.

## JavaScript

JavaScript is the real-world implementation of ECMAScript plus host APIs.

Browser JavaScript includes:

- DOM APIs.
- Events.
- Fetch.
- Web Storage.
- Timers.
- History.
- Location.
- Web Workers.

Node.js JavaScript includes:

- File system APIs.
- HTTP server APIs.
- Process APIs.
- Streams.
- Buffers.

Interview answer:

```text
ECMAScript defines the core language: syntax, types, objects, promises, modules, etc. JavaScript in a browser also includes Web APIs like DOM, fetch, localStorage, and events. Node.js adds its own runtime APIs like fs, process, and streams.
```

---

# 3. Version Timeline And Enhancements

## ES5

Important additions:

- Strict mode.
- `Array.prototype.map/filter/reduce/forEach`.
- `Object.create`.
- `Object.keys`.
- `Function.prototype.bind`.
- JSON support.

Example:

```js
"use strict";

const names = users
  .filter(function (user) {
    return user.active;
  })
  .map(function (user) {
    return user.name;
  });
```

## ES6 / ES2015

The biggest modern JavaScript release.

Important additions:

- `let`, `const`.
- Arrow functions.
- Template literals.
- Destructuring.
- Default parameters.
- Rest/spread.
- Classes.
- Modules.
- Promises.
- `Map`, `Set`, `WeakMap`, `WeakSet`.
- Symbols.
- Iterators/generators.

Example:

```js
const getActiveNames = (users = []) =>
  users.filter((user) => user.active).map((user) => user.name);
```

## ES2016

Important additions:

- `Array.prototype.includes`.
- Exponentiation operator `**`.

```js
[1, 2, 3].includes(2); // true
2 ** 3; // 8
```

## ES2017

Important additions:

- `async` / `await`.
- `Object.values`.
- `Object.entries`.
- String padding.

```js
async function loadUser(id) {
  const response = await fetch(`/api/users/${id}`);
  return response.json();
}
```

## ES2018

Important additions:

- Object rest/spread.
- Promise `finally`.
- Async iteration.
- RegExp improvements.

```js
const { password, ...safeUser } = user;

fetchData()
  .then(handleData)
  .catch(handleError)
  .finally(hideSpinner);
```

## ES2019

Important additions:

- `Array.prototype.flat`.
- `Array.prototype.flatMap`.
- `Object.fromEntries`.
- Optional catch binding.
- Stable `sort`.

```js
const params = Object.fromEntries(new URLSearchParams(location.search));
```

## ES2020

Important additions:

- Optional chaining `?.`.
- Nullish coalescing `??`.
- `Promise.allSettled`.
- `BigInt`.
- `globalThis`.
- Dynamic import.

```js
const city = user.address?.city ?? "Unknown";

const results = await Promise.allSettled([fetchUsers(), fetchOrders()]);
```

## ES2021

Important additions:

- Logical assignment operators.
- `Promise.any`.
- `String.prototype.replaceAll`.
- Numeric separators.

```js
settings.theme ??= "light";

const amount = 10_00_000;

"a-b-c".replaceAll("-", "_"); // a_b_c
```

## ES2022

Important additions:

- Class fields.
- Private fields.
- Static blocks.
- Top-level await.
- `Object.hasOwn`.
- `Error.cause`.
- `.at()`.

```js
class Counter {
  #count = 0;

  increment() {
    this.#count += 1;
  }
}

const last = items.at(-1);
```

## ES2023

Important additions:

- Change-array-by-copy methods: `toSorted`, `toReversed`, `toSpliced`, `with`.
- `findLast`, `findLastIndex`.
- Symbols as WeakMap keys.

```js
const sortedUsers = users.toSorted((a, b) => a.name.localeCompare(b.name));

const lastAdmin = users.findLast((user) => user.role === "admin");
```

React note:

```text
Prefer toSorted/toReversed/toSpliced over mutating sort/reverse/splice when updating React state.
```

## ES2024

Important additions:

- `Object.groupBy`.
- `Map.groupBy`.
- `Promise.withResolvers`.
- Resizable/growable ArrayBuffers.
- RegExp `v` flag.
- `ArrayBuffer.prototype.transfer`.

```js
const usersByRole = Object.groupBy(users, (user) => user.role);
```

## ES2025

Important additions:

- Iterator helpers.
- New `Set` methods.
- `RegExp.escape`.
- `Promise.try`.
- JSON modules.
- Import attributes.
- RegExp modifiers.
- Duplicate named capture groups.
- `Float16Array`, `DataView` float16 helpers, `Math.f16round`.

Examples:

```js
const common = new Set([1, 2, 3]).intersection(new Set([2, 3, 4]));
// Set {2, 3}

const escaped = RegExp.escape("user.name@example.com");
const regex = new RegExp(escaped);

const result = await Promise.try(() => maybeSyncOrAsync());
```

Iterator helpers:

```js
const result = numbers
  .values()
  .filter((number) => number % 2 === 0)
  .map((number) => number * 10)
  .take(3)
  .toArray();
```

## ES2026 Expected Finished Proposals

TC39 finished proposals list these with expected publication year 2026:

- Upsert APIs.
- JSON parse source text access.
- Iterator sequencing.
- `Uint8Array` to/from Base64.
- `Math.sumPrecise`.
- `Error.isError`.
- `Array.fromAsync`.

Example:

```js
const array = await Array.fromAsync(asyncIterable);

if (Error.isError(error)) {
  console.error(error.message);
}
```

Note:

```text
For interview answers, separate stable/current features from proposal features. Browser/runtime support may lag behind the specification, so check compatibility before using the newest APIs in production.
```

---

# 4. Variables: `var`, `let`, `const`

## `var`

- Function scoped.
- Hoisted and initialized with `undefined`.
- Can be redeclared.
- Avoid in modern code.

```js
function test() {
  console.log(name); // undefined
  var name = "Anjali";
}
```

## `let`

- Block scoped.
- Can be reassigned.
- Hoisted but in temporal dead zone until declaration.

```js
let count = 1;
count = 2;
```

## `const`

- Block scoped.
- Cannot be reassigned.
- Object/array contents can still mutate.

```js
const user = { name: "Anjali" };
user.name = "AJ"; // allowed

// user = {}; // not allowed
```

Interview answer:

```text
I use const by default, let when reassignment is needed, and avoid var. const prevents rebinding, not object mutation.
```

---

# 5. Data Types

## Primitive Types

- `string`
- `number`
- `bigint`
- `boolean`
- `undefined`
- `symbol`
- `null`

## Non-Primitive Type

- `object`

Arrays, functions, dates, maps, sets, and regex are objects.

```js
typeof "hello"; // string
typeof 123; // number
typeof true; // boolean
typeof undefined; // undefined
typeof Symbol("id"); // symbol
typeof 10n; // bigint
typeof {}; // object
typeof []; // object
typeof function () {}; // function
typeof null; // object, historical bug
```

Check arrays:

```js
Array.isArray([]); // true
```

Check null:

```js
value === null;
```

---

# 6. Equality

## `==`

Loose equality with type coercion.

```js
0 == false; // true
"5" == 5; // true
null == undefined; // true
```

## `===`

Strict equality without type coercion.

```js
0 === false; // false
"5" === 5; // false
null === undefined; // false
```

Use `===` by default.

Special cases:

```js
NaN === NaN; // false
Object.is(NaN, NaN); // true
Object.is(0, -0); // false
```

Interview answer:

```text
Use strict equality unless there is a deliberate reason for coercion. For nullish checks, value == null is sometimes intentionally used to match both null and undefined, but it should be explicit team convention.
```

---

# 7. Truthy And Falsy

Falsy values:

```js
false
0
-0
0n
""
null
undefined
NaN
```

Everything else is truthy, including:

```js
[]
{}
"0"
"false"
function () {}
```

Example:

```js
if ([]) {
  console.log("array is truthy");
}
```

React pitfall:

```jsx
{items.length && <List items={items} />}
```

If length is `0`, React may render `0`.

Better:

```jsx
{items.length > 0 && <List items={items} />}
```

---

# 8. Type Coercion

JavaScript converts types automatically in some operations.

```js
"5" + 1; // "51"
"5" - 1; // 4
true + 1; // 2
Number("10"); // 10
String(10); // "10"
Boolean(""); // false
```

Interview tip:

```text
Avoid relying on implicit coercion in business code. Convert explicitly with Number, String, Boolean, parseInt, or parseFloat.
```

`parseInt`:

```js
parseInt("10px", 10); // 10
Number("10px"); // NaN
```

---

# 9. Scope

## Global Scope

Available everywhere.

```js
const appName = "Dashboard";
```

## Function Scope

```js
function greet() {
  const message = "Hello";
  console.log(message);
}
```

## Block Scope

```js
if (true) {
  let count = 1;
  const name = "Anjali";
}
```

## Lexical Scope

Inner functions can access variables from outer functions.

```js
function outer() {
  const name = "Anjali";

  function inner() {
    console.log(name);
  }

  inner();
}
```

---

# 10. Hoisting And Temporal Dead Zone

Function declarations are hoisted:

```js
sayHi();

function sayHi() {
  console.log("Hi");
}
```

`var` is hoisted as `undefined`:

```js
console.log(name); // undefined
var name = "Anjali";
```

`let` and `const` are hoisted but unavailable before declaration:

```js
console.log(name); // ReferenceError
const name = "Anjali";
```

This unavailable period is called temporal dead zone.

---

# 11. Closures

A closure is when a function remembers variables from its lexical scope even after the outer function has finished.

```js
function createCounter() {
  let count = 0;

  return function increment() {
    count += 1;
    return count;
  };
}

const counter = createCounter();
counter(); // 1
counter(); // 2
```

Use cases:

- Data privacy.
- Function factories.
- Memoization.
- Event handlers.
- React hooks.

React closure pitfall:

```jsx
function Counter() {
  const [count, setCount] = useState(0);

  function delayedIncrement() {
    setTimeout(() => {
      setCount(count + 1); // may use stale count
    }, 1000);
  }
}
```

Fix:

```jsx
setCount((current) => current + 1);
```

Interview answer:

```text
Closure means an inner function keeps access to outer variables. It is useful but can cause stale values in async callbacks, especially in React event handlers and effects.
```

---

# 12. Functions

## Function Declaration

```js
function add(a, b) {
  return a + b;
}
```

Hoisted.

## Function Expression

```js
const add = function (a, b) {
  return a + b;
};
```

Not callable before initialization.

## Arrow Function

```js
const add = (a, b) => a + b;
```

Differences:

- No own `this`.
- No own `arguments`.
- Cannot be used as constructor.
- Good for callbacks.

```js
const user = {
  name: "Anjali",
  normal() {
    console.log(this.name);
  },
  arrow: () => {
    console.log(this.name);
  },
};
```

Use normal method syntax for object methods that need `this`.

## Default Parameters

```js
function greet(name = "Guest") {
  return `Hello ${name}`;
}
```

## Rest Parameters

```js
function sum(...numbers) {
  return numbers.reduce((total, number) => total + number, 0);
}
```

## Higher-Order Function

A function that receives or returns another function.

```js
function withLogger(fn) {
  return function (...args) {
    console.log("Calling function");
    return fn(...args);
  };
}
```

---

# 13. `this`

`this` is determined by how a function is called.

## Global / Regular Function

```js
function showThis() {
  console.log(this);
}
```

In strict mode, `this` is `undefined`. In non-strict browser code, it may be `window`.

## Object Method

```js
const user = {
  name: "Anjali",
  greet() {
    console.log(this.name);
  },
};

user.greet(); // Anjali
```

## Lost `this`

```js
const greet = user.greet;
greet(); // this lost
```

Fix:

```js
const greet = user.greet.bind(user);
```

## Arrow Function

Arrow functions capture `this` from surrounding lexical scope.

```js
function Timer() {
  this.seconds = 0;

  setInterval(() => {
    this.seconds += 1;
  }, 1000);
}
```

Interview answer:

```text
Normal functions get this from call-site. Arrow functions do not have their own this; they use lexical this. In React function components, this is usually irrelevant, but in classes and object methods it matters.
```

---

# 14. Objects

## Object Basics

```js
const user = {
  id: 1,
  name: "Anjali",
  active: true,
};

user.email = "a@example.com";
delete user.active;
```

## Access

```js
user.name;
user["name"];
```

Dynamic key:

```js
const key = "name";
user[key];
```

## Computed Property

```js
const field = "email";

const form = {
  [field]: "a@example.com",
};
```

React form pattern:

```jsx
function handleChange(event) {
  const { name, value } = event.target;

  setForm((form) => ({
    ...form,
    [name]: value,
  }));
}
```

## Object Methods

```js
Object.keys(user);
Object.values(user);
Object.entries(user);
Object.fromEntries([["name", "Anjali"]]);
Object.hasOwn(user, "name");
```

## Shallow Copy

```js
const copy = { ...user };
```

Nested objects are still shared:

```js
const state = { user: { name: "A" } };
const next = { ...state };

next.user.name = "B";
console.log(state.user.name); // B
```

Deep clone options:

```js
const clone = structuredClone(value);
```

Use carefully because not every value is cloneable.

---

# 15. Arrays

Arrays are ordered objects.

```js
const items = ["a", "b", "c"];
items[0]; // a
items.length; // 3
```

## Method Return Cheat Sheet

| Method | Returns | Mutates? | Use case |
|---|---|---|---|
| `map()` | New array | No | Transform every item |
| `filter()` | New array | No | Keep matching items |
| `find()` | Item or `undefined` | No | Get first matching item |
| `findIndex()` | Index or `-1` | No | Find matching position |
| `some()` | Boolean | No | At least one matches |
| `every()` | Boolean | No | All match |
| `reduce()` | Any final value | No | Total, group, object |
| `forEach()` | `undefined` | No | Side effects |
| `includes()` | Boolean | No | Value exists |
| `slice()` | New array | No | Copy part |
| `splice()` | Removed items | Yes | Add/remove/replace |
| `sort()` | Same array | Yes | Sort original |
| `reverse()` | Same array | Yes | Reverse original |
| `toSorted()` | New array | No | Sort copy |
| `toReversed()` | New array | No | Reverse copy |
| `toSpliced()` | New array | No | Splice copy |
| `with()` | New array | No | Replace index immutably |
| `flat()` | New array | No | Flatten |
| `flatMap()` | New array | No | Map then flatten one level |
| `push()` | New length | Yes | Add end |
| `pop()` | Removed item | Yes | Remove end |
| `unshift()` | New length | Yes | Add start |
| `shift()` | Removed item | Yes | Remove start |

## Common Combinations

```js
const activeNames = users
  .filter((user) => user.active)
  .map((user) => user.name);

const admin = users.find((user) => user.role === "admin");

const hasAdmin = users.some((user) => user.role === "admin");

const allVerified = users.every((user) => user.verified);

const total = cart.reduce((sum, item) => sum + item.price * item.qty, 0);

const usersByRole = users.reduce((acc, user) => {
  acc[user.role] ??= [];
  acc[user.role].push(user);
  return acc;
}, {});
```

## React State Array Updates

Add:

```js
setItems((items) => [...items, newItem]);
```

Remove:

```js
setItems((items) => items.filter((item) => item.id !== id));
```

Update:

```js
setItems((items) =>
  items.map((item) =>
    item.id === id ? { ...item, completed: true } : item
  )
);
```

Sort without mutation:

```js
setUsers((users) => users.toSorted((a, b) => a.name.localeCompare(b.name)));
```

If `toSorted` is unavailable:

```js
setUsers((users) => [...users].sort((a, b) => a.name.localeCompare(b.name)));
```

---

# 16. Destructuring, Spread, Rest

## Object Destructuring

```js
const { name, email } = user;
```

Rename:

```js
const { name: userName } = user;
```

Default:

```js
const { role = "user" } = user;
```

## Array Destructuring

```js
const [first, second] = items;
```

React hook style:

```js
const [count, setCount] = useState(0);
```

## Spread

```js
const nextUser = { ...user, name: "New Name" };
const nextItems = [...items, newItem];
```

## Rest

```js
const { password, ...safeUser } = user;

function log(level, ...messages) {
  console.log(level, messages);
}
```

---

# 17. Template Literals

```js
const message = `Hello ${user.name}`;
```

Multiline:

```js
const html = `
  <section>
    <h1>${title}</h1>
  </section>
`;
```

Use carefully with user input to avoid injection issues when writing raw HTML.

---

# 18. Optional Chaining And Nullish Coalescing

## Optional Chaining

```js
const city = user.address?.city;
```

If `address` is nullish, result is `undefined`.

## Nullish Coalescing

```js
const page = inputPage ?? 1;
```

Only uses default when left side is `null` or `undefined`.

Difference:

```js
0 || 10; // 10
0 ?? 10; // 0

"" || "default"; // default
"" ?? "default"; // ""
```

React prop default:

```jsx
function Badge({ count }) {
  return <span>{count ?? 0}</span>;
}
```

---

# 19. Promises

A promise represents a future value.

States:

- Pending.
- Fulfilled.
- Rejected.

```js
fetch("/api/users")
  .then((response) => response.json())
  .then((users) => console.log(users))
  .catch((error) => console.error(error))
  .finally(() => console.log("done"));
```

## Promise Utilities

### `Promise.all`

Fails fast if one promise rejects.

```js
const [users, orders] = await Promise.all([
  fetchUsers(),
  fetchOrders(),
]);
```

### `Promise.allSettled`

Waits for all, even failures.

```js
const results = await Promise.allSettled([fetchUsers(), fetchOrders()]);
```

### `Promise.race`

Settles when first promise settles.

```js
const result = await Promise.race([request, timeout]);
```

### `Promise.any`

Fulfilled when first promise fulfills. Rejects only if all reject.

```js
const fastestSuccess = await Promise.any([cdn1(), cdn2(), cdn3()]);
```

### `Promise.withResolvers`

Useful when promise resolve/reject must be controlled externally.

```js
const { promise, resolve, reject } = Promise.withResolvers();
```

### `Promise.try`

Normalizes sync or async function into a promise.

```js
const result = await Promise.try(() => maybeSyncOrAsync());
```

---

# 20. Async/Await

`async/await` is syntax over promises.

```js
async function loadUsers() {
  try {
    const response = await fetch("/api/users");

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error(error);
    throw error;
  }
}
```

Parallel vs sequential:

```js
// Sequential
const users = await fetchUsers();
const orders = await fetchOrders();

// Parallel
const [usersResult, ordersResult] = await Promise.all([
  fetchUsers(),
  fetchOrders(),
]);
```

Interview answer:

```text
await pauses the async function, not the whole JavaScript thread. Other tasks can continue through the event loop.
```

---

# 21. Event Loop

JavaScript uses:

- Call stack.
- Web APIs / runtime APIs.
- Task queue.
- Microtask queue.
- Event loop.

Order:

```js
console.log("A");

setTimeout(() => console.log("B"), 0);

Promise.resolve().then(() => console.log("C"));

console.log("D");
```

Output:

```text
A
D
C
B
```

Why:

- Synchronous code runs first.
- Promise callbacks are microtasks.
- `setTimeout` callback is a task/macrotask.
- Microtasks run before next task.

React relevance:

```text
React state updates, batching, timers, promises, and async effects all rely on event loop behavior. Understanding stale closures and microtask timing helps debug UI issues.
```

---

# 22. Timers

## `setTimeout`

```js
const id = setTimeout(() => {
  console.log("after 1 second");
}, 1000);

clearTimeout(id);
```

## `setInterval`

```js
const id = setInterval(() => {
  console.log("every second");
}, 1000);

clearInterval(id);
```

React cleanup:

```jsx
useEffect(() => {
  const id = setInterval(() => {
    setSeconds((seconds) => seconds + 1);
  }, 1000);

  return () => clearInterval(id);
}, []);
```

---

# 23. Debounce And Throttle

## Debounce

Run after user stops triggering event.

Use cases:

- Search input.
- Resize after user stops.
- Autosave after typing stops.

```js
function debounce(fn, delay) {
  let timerId;

  return function (...args) {
    clearTimeout(timerId);

    timerId = setTimeout(() => {
      fn.apply(this, args);
    }, delay);
  };
}
```

## Throttle

Run at most once per time window.

Use cases:

- Scroll tracking.
- Mouse move.
- Resize during continuous action.

```js
function throttle(fn, delay) {
  let waiting = false;

  return function (...args) {
    if (waiting) return;

    fn.apply(this, args);
    waiting = true;

    setTimeout(() => {
      waiting = false;
    }, delay);
  };
}
```

React hook debounce:

```jsx
function useDebouncedValue(value, delay) {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    const id = setTimeout(() => setDebouncedValue(value), delay);
    return () => clearTimeout(id);
  }, [value, delay]);

  return debouncedValue;
}
```

---

# 24. Modules

## Named Export

```js
export function add(a, b) {
  return a + b;
}

export const PI = 3.14;
```

Import:

```js
import { add, PI } from "./math.js";
```

## Default Export

```js
export default function Button() {}
```

Import:

```js
import Button from "./Button.js";
```

## Dynamic Import

```js
const module = await import("./heavy-module.js");
```

React lazy:

```jsx
const AdminPage = lazy(() => import("./AdminPage"));
```

## JSON Modules With Import Attributes

```js
import config from "./config.json" with { type: "json" };
```

Check runtime/bundler support before using directly.

---

# 25. Classes And Prototypes

JavaScript inheritance is prototype-based. Classes are syntax over prototypes.

## Constructor Function

```js
function User(name) {
  this.name = name;
}

User.prototype.greet = function () {
  return `Hello ${this.name}`;
};
```

## Class Syntax

```js
class User {
  #token;

  constructor(name, token) {
    this.name = name;
    this.#token = token;
  }

  greet() {
    return `Hello ${this.name}`;
  }
}
```

## Prototype Chain

```js
const user = new User("Anjali", "secret");

user.__proto__ === User.prototype; // true, avoid using __proto__ in app code
```

Better:

```js
Object.getPrototypeOf(user) === User.prototype;
```

Interview answer:

```text
Every object can have a prototype. If a property is not found on the object, JavaScript looks up the prototype chain. Class syntax is a cleaner way to create constructor/prototype behavior.
```

---

# 26. `call`, `apply`, `bind`

```js
function greet(greeting, punctuation) {
  return `${greeting} ${this.name}${punctuation}`;
}

const user = { name: "Anjali" };

greet.call(user, "Hello", "!");
greet.apply(user, ["Hello", "!"]);

const greetUser = greet.bind(user);
greetUser("Hi", "!");
```

Difference:

- `call`: invokes with arguments one by one.
- `apply`: invokes with arguments array.
- `bind`: returns a new function with bound `this`.

---

# 27. Map, Set, WeakMap, WeakSet

## Set

Unique values.

```js
const uniqueRoles = new Set(users.map((user) => user.role));
```

ES2025 Set methods:

```js
const a = new Set([1, 2, 3]);
const b = new Set([3, 4, 5]);

a.union(b); // Set {1, 2, 3, 4, 5}
a.intersection(b); // Set {3}
a.difference(b); // Set {1, 2}
a.isDisjointFrom(b); // false
```

## Map

Key-value pairs where keys can be any value.

```js
const userMap = new Map();
userMap.set(user.id, user);
userMap.get(user.id);
```

## WeakMap

Keys must be objects and are weakly held, allowing garbage collection.

```js
const privateData = new WeakMap();

class User {
  constructor(secret) {
    privateData.set(this, { secret });
  }
}
```

Use cases:

- Metadata for objects.
- Caches without preventing garbage collection.

---

# 28. Error Handling

```js
try {
  riskyOperation();
} catch (error) {
  console.error(error);
} finally {
  cleanup();
}
```

Custom error:

```js
class ApiError extends Error {
  constructor(message, status, options) {
    super(message, options);
    this.name = "ApiError";
    this.status = status;
  }
}

throw new ApiError("Unauthorized", 401, {
  cause: originalError,
});
```

Async errors:

```js
async function submit() {
  try {
    await save();
  } catch (error) {
    showError(error.message);
  }
}
```

React note:

```text
Error boundaries catch render errors, not normal event handler errors. Event handler errors should be handled with try/catch or promise catch.
```

---

# 29. Fetch API

```js
async function getUser(id) {
  const response = await fetch(`/api/users/${id}`);

  if (!response.ok) {
    throw new Error(`Request failed: ${response.status}`);
  }

  return response.json();
}
```

POST:

```js
async function createUser(user) {
  const response = await fetch("/api/users", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(user),
  });

  if (!response.ok) {
    throw new Error("Create failed");
  }

  return response.json();
}
```

Abort:

```js
const controller = new AbortController();

fetch("/api/users", { signal: controller.signal });

controller.abort();
```

React effect:

```jsx
useEffect(() => {
  const controller = new AbortController();

  async function load() {
    const response = await fetch(`/api/users/${userId}`, {
      signal: controller.signal,
    });
    setUser(await response.json());
  }

  load().catch((error) => {
    if (error.name !== "AbortError") {
      setError(error);
    }
  });

  return () => controller.abort();
}, [userId]);
```

---

# 30. Browser Storage

## localStorage

- Persists until removed.
- String-only.
- Synchronous.

```js
localStorage.setItem("theme", "dark");
const theme = localStorage.getItem("theme");
localStorage.removeItem("theme");
```

Store object:

```js
localStorage.setItem("user", JSON.stringify(user));
const user = JSON.parse(localStorage.getItem("user") ?? "null");
```

## sessionStorage

Persists for browser tab session.

## Cookies

Sent with HTTP requests. Useful for server sessions, auth tokens with `HttpOnly`, etc.

Security note:

```text
Do not store sensitive access tokens in localStorage if XSS risk is a concern. HttpOnly secure cookies reduce token theft from JavaScript, but need CSRF strategy.
```

---

# 31. DOM And Events

## Select Element

```js
const button = document.querySelector("#save");
```

## Add Event Listener

```js
button.addEventListener("click", handleClick);
button.removeEventListener("click", handleClick);
```

## Event Bubbling

Events bubble from target up to ancestors.

```html
<div id="parent">
  <button id="child">Click</button>
</div>
```

```js
parent.addEventListener("click", () => console.log("parent"));
child.addEventListener("click", () => console.log("child"));
```

Click child:

```text
child
parent
```

Stop propagation:

```js
event.stopPropagation();
```

Prevent default:

```js
event.preventDefault();
```

## Event Delegation

Attach one listener to parent.

```js
document.querySelector("#list").addEventListener("click", (event) => {
  const item = event.target.closest("[data-id]");

  if (!item) return;

  console.log(item.dataset.id);
});
```

React relevance:

```text
React uses a synthetic event system and event delegation internally. Modern React attaches listeners to the root container rather than the whole document.
```

---

# 32. Immutability

Immutability means create new values instead of mutating existing ones.

Important for React:

```text
React compares references to detect changes. Mutating existing state can prevent proper re-render or create bugs.
```

Bad:

```js
state.user.name = "New Name";
setState(state);
```

Good:

```js
setState((state) => ({
  ...state,
  user: {
    ...state.user,
    name: "New Name",
  },
}));
```

Array:

```js
setItems((items) => items.filter((item) => item.id !== id));
```

---

# 33. Copy: Shallow vs Deep

## Shallow Copy

Copies only first level.

```js
const copy = { ...user };
const listCopy = [...items];
```

## Deep Copy

Copies nested values.

```js
const copy = structuredClone(data);
```

JSON clone problems:

```js
JSON.parse(JSON.stringify(data));
```

Loses:

- Dates.
- `undefined`.
- Functions.
- Maps/Sets.
- BigInt.
- Some special values.

---

# 34. `JSON`

```js
const text = JSON.stringify({ name: "Anjali" });
const object = JSON.parse(text);
```

Safe parse:

```js
function safeJsonParse(text, fallback = null) {
  try {
    return JSON.parse(text);
  } catch {
    return fallback;
  }
}
```

---

# 35. Regular Expressions

```js
const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
emailPattern.test("a@example.com"); // true
```

Match:

```js
const match = "Order #123".match(/#(\d+)/);
match?.[1]; // 123
```

Named groups:

```js
const match = "2026-06-26".match(
  /(?<year>\d{4})-(?<month>\d{2})-(?<day>\d{2})/
);

match.groups.year; // 2026
```

Escape user input:

```js
const input = "a+b?";
const regex = new RegExp(RegExp.escape(input));
```

Without escaping, user input can change regex meaning.

---

# 36. Date And Time

Basic Date:

```js
const now = new Date();
now.toISOString();
```

Common issue:

```text
Date parsing and timezone handling are easy to get wrong. For serious apps, use ISO strings, store UTC, and format based on user locale/timezone.
```

Format:

```js
const formatter = new Intl.DateTimeFormat("en-IN", {
  dateStyle: "medium",
  timeStyle: "short",
});

formatter.format(new Date());
```

Temporal note:

```text
Temporal is a modern date/time API in TC39 finished proposals with expected publication after ES2026. Check runtime support before using without polyfill.
```

---

# 37. Internationalization

## Number Formatting

```js
const price = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
}).format(123456.78);
```

## Date Formatting

```js
new Intl.DateTimeFormat("en-IN").format(new Date());
```

## Collator For Sorting

```js
const collator = new Intl.Collator("en");
const sorted = names.toSorted((a, b) => collator.compare(a, b));
```

---

# 38. Memory Management

JavaScript uses garbage collection.

Memory leaks happen when references are kept unnecessarily.

Common browser leaks:

- Not removing event listeners.
- Un-cleared intervals/timeouts.
- Large objects kept in closures.
- Global caches that never clear.
- Detached DOM nodes still referenced.

React cleanup:

```jsx
useEffect(() => {
  window.addEventListener("resize", handleResize);

  return () => {
    window.removeEventListener("resize", handleResize);
  };
}, []);
```

---

# 39. Security Basics

## XSS

Cross-site scripting happens when untrusted content runs as JavaScript.

Bad:

```js
element.innerHTML = userInput;
```

Better:

```js
element.textContent = userInput;
```

React escapes values by default:

```jsx
<p>{userInput}</p>
```

Dangerous:

```jsx
<div dangerouslySetInnerHTML={{ __html: userInput }} />
```

Use only with sanitized trusted HTML.

## CSRF

Cross-site request forgery matters when using cookies for auth. Use SameSite cookies, CSRF tokens, and proper server-side validation.

## Prototype Pollution

Avoid blindly merging untrusted objects.

```js
Object.assign(target, untrustedInput);
```

Validate keys like `__proto__`, `constructor`, `prototype`.

---

# 40. Performance Basics

## Avoid Blocking Main Thread

Bad for large data:

```js
const result = hugeArray.map(expensiveWork);
```

Options:

- Chunk work.
- Use Web Worker.
- Virtualize UI lists.
- Move heavy work to backend.
- Memoize expensive pure calculations.

## Web Worker Concept

```js
const worker = new Worker("/worker.js");
worker.postMessage(data);
worker.onmessage = (event) => {
  console.log(event.data);
};
```

## Measure First

Use:

- Browser Performance tab.
- Lighthouse.
- React DevTools Profiler.
- Network tab.

---

# 41. JavaScript For React Interviews

## State Update Depends On Previous State

```jsx
setCount((count) => count + 1);
```

Use functional update to avoid stale closure bugs.

## Do Not Mutate State

```jsx
setUser((user) => ({ ...user, name: "New Name" }));
```

## Keys Need Stable Identity

```jsx
users.map((user) => <UserRow key={user.id} user={user} />);
```

Avoid index key for dynamic lists.

## Controlled Input

```jsx
function SearchBox() {
  const [query, setQuery] = useState("");

  return (
    <input
      value={query}
      onChange={(event) => setQuery(event.target.value)}
    />
  );
}
```

## Derived Data

Avoid storing derived data in state:

```jsx
const activeUsers = users.filter((user) => user.active);
```

Use `useMemo` only when expensive or reference stability matters:

```jsx
const activeUsers = useMemo(
  () => users.filter((user) => user.active),
  [users]
);
```

## Async Effect With Cleanup

```jsx
useEffect(() => {
  const controller = new AbortController();

  async function load() {
    const response = await fetch(url, { signal: controller.signal });
    setData(await response.json());
  }

  load().catch((error) => {
    if (error.name !== "AbortError") {
      setError(error);
    }
  });

  return () => controller.abort();
}, [url]);
```

## Event Handler vs Function Call

Bad:

```jsx
<button onClick={saveUser()}>Save</button>
```

Good:

```jsx
<button onClick={saveUser}>Save</button>
<button onClick={() => saveUser(user.id)}>Save</button>
```

## Conditional Rendering Pitfall

Bad:

```jsx
{items.length && <List items={items} />}
```

Good:

```jsx
{items.length > 0 && <List items={items} />}
```

---

# 42. Implementation Tricks

## Trick 1: Group By

Modern:

```js
const byRole = Object.groupBy(users, (user) => user.role);
```

Compatible reducer:

```js
const byRole = users.reduce((acc, user) => {
  acc[user.role] ??= [];
  acc[user.role].push(user);
  return acc;
}, {});
```

## Trick 2: Unique Array

```js
const unique = [...new Set(items)];
```

Unique by id:

```js
const uniqueUsers = [...new Map(users.map((user) => [user.id, user])).values()];
```

## Trick 3: Count By Field

```js
const countByRole = users.reduce((acc, user) => {
  acc[user.role] = (acc[user.role] ?? 0) + 1;
  return acc;
}, {});
```

## Trick 4: Safe Nested Access

```js
const city = user?.address?.city ?? "Unknown";
```

## Trick 5: Remove Empty Values

```js
const clean = Object.fromEntries(
  Object.entries(form).filter(([, value]) => value != null && value !== "")
);
```

## Trick 6: Query String To Object

```js
const params = Object.fromEntries(new URLSearchParams(location.search));
```

## Trick 7: Object To Query String

```js
const query = new URLSearchParams({
  page: "1",
  status: "active",
}).toString();
```

## Trick 8: Retry Async Function

```js
async function retry(fn, retries = 3) {
  let lastError;

  for (let attempt = 1; attempt <= retries; attempt += 1) {
    try {
      return await fn();
    } catch (error) {
      lastError = error;
    }
  }

  throw lastError;
}
```

## Trick 9: Sleep

```js
const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

await sleep(500);
```

## Trick 10: Timeout Promise

```js
function withTimeout(promise, ms) {
  const timeout = new Promise((_, reject) => {
    setTimeout(() => reject(new Error("Timeout")), ms);
  });

  return Promise.race([promise, timeout]);
}
```

## Trick 11: Limit Concurrency

```js
async function runWithLimit(tasks, limit) {
  const results = [];
  const executing = new Set();

  for (const task of tasks) {
    const promise = Promise.resolve().then(task);
    results.push(promise);
    executing.add(promise);

    promise.finally(() => executing.delete(promise));

    if (executing.size >= limit) {
      await Promise.race(executing);
    }
  }

  return Promise.all(results);
}
```

## Trick 12: Deep Freeze For Dev

```js
function deepFreeze(object) {
  Object.freeze(object);

  Object.values(object).forEach((value) => {
    if (value && typeof value === "object" && !Object.isFrozen(value)) {
      deepFreeze(value);
    }
  });

  return object;
}
```

Useful to catch accidental mutations in development.

---

# 43. Common Interview Questions

## What Is Closure?

A closure is when a function remembers variables from its lexical scope even after the outer function returns.

```js
function createMultiplier(factor) {
  return (value) => value * factor;
}

const double = createMultiplier(2);
double(5); // 10
```

## What Is Hoisting?

Hoisting means declarations are processed before code execution. `var` is initialized with `undefined`; `let` and `const` stay in temporal dead zone; function declarations are callable before their definition.

## `var` vs `let` vs `const`

`var` is function-scoped and redeclarable. `let` and `const` are block-scoped. `const` cannot be reassigned. Use `const` by default and `let` for reassignment.

## Arrow Function vs Normal Function

Arrow functions do not have their own `this`, `arguments`, or constructor behavior. Normal functions have dynamic `this` based on call-site.

## What Is Event Loop?

The event loop coordinates synchronous stack execution, microtasks like promises, and tasks like timers/events. Synchronous code runs first, microtasks next, then tasks.

## Promise vs Async/Await

`async/await` is syntax built on promises. It makes async code look sequential but still returns promises.

## `Promise.all` vs `Promise.allSettled`

`Promise.all` rejects as soon as one promise rejects. `Promise.allSettled` waits for all promises and gives success/failure result for each.

## Shallow Copy vs Deep Copy

Shallow copy copies top-level references. Deep copy recursively copies nested data. Spread creates shallow copy.

## `null` vs `undefined`

`undefined` usually means value not assigned. `null` is intentional absence of value.

## `==` vs `===`

`==` allows type coercion. `===` checks value and type without coercion. Use `===` by default.

## `map` vs `forEach`

`map` returns a new array. `forEach` returns `undefined` and is used for side effects.

## `filter` vs `find`

`filter` returns all matching items as an array. `find` returns first matching item or `undefined`.

## `slice` vs `splice`

`slice` returns a copy and does not mutate. `splice` mutates the original array.

## `call` vs `apply` vs `bind`

`call` invokes with individual args. `apply` invokes with array args. `bind` returns a new bound function.

## LocalStorage vs SessionStorage vs Cookies

`localStorage` persists until removed. `sessionStorage` persists per tab session. Cookies are sent with requests and can be HttpOnly/Secure/SameSite.

## What Is Prototype?

Prototype is an object used for inheritance. When a property is not found directly, JavaScript checks the prototype chain.

## What Is Currying?

Currying transforms a function with multiple arguments into chained unary functions.

```js
const add = (a) => (b) => a + b;
add(2)(3); // 5
```

## What Is Memoization?

Memoization caches function results.

```js
function memoize(fn) {
  const cache = new Map();

  return function (value) {
    if (cache.has(value)) return cache.get(value);

    const result = fn(value);
    cache.set(value, result);
    return result;
  };
}
```

## What Is Debouncing?

Debouncing delays execution until events stop firing for a given time.

## What Is Throttling?

Throttling limits execution to once per time interval.

---

# 44. Common Coding Questions

## Reverse String

```js
function reverseString(value) {
  return [...value].reverse().join("");
}
```

## Check Palindrome

```js
function isPalindrome(value) {
  const normalized = value.toLowerCase().replace(/[^a-z0-9]/g, "");
  return normalized === [...normalized].reverse().join("");
}
```

## Flatten Array

```js
function flatten(array) {
  return array.reduce((acc, item) => {
    return acc.concat(Array.isArray(item) ? flatten(item) : item);
  }, []);
}
```

Modern:

```js
array.flat(Infinity);
```

## Deep Equal Basic

```js
function deepEqual(a, b) {
  if (Object.is(a, b)) return true;

  if (
    typeof a !== "object" ||
    typeof b !== "object" ||
    a === null ||
    b === null
  ) {
    return false;
  }

  const keysA = Object.keys(a);
  const keysB = Object.keys(b);

  if (keysA.length !== keysB.length) return false;

  return keysA.every((key) => deepEqual(a[key], b[key]));
}
```

## Deep Clone Basic

```js
function deepClone(value) {
  if (value === null || typeof value !== "object") {
    return value;
  }

  if (Array.isArray(value)) {
    return value.map(deepClone);
  }

  return Object.fromEntries(
    Object.entries(value).map(([key, nestedValue]) => [
      key,
      deepClone(nestedValue),
    ])
  );
}
```

Use `structuredClone` when supported and suitable.

## Once Function

```js
function once(fn) {
  let called = false;
  let result;

  return function (...args) {
    if (!called) {
      called = true;
      result = fn.apply(this, args);
    }

    return result;
  };
}
```

## Compose

```js
const compose =
  (...functions) =>
  (value) =>
    functions.reduceRight((result, fn) => fn(result), value);

const add1 = (x) => x + 1;
const double = (x) => x * 2;

compose(double, add1)(3); // 8
```

## Pipe

```js
const pipe =
  (...functions) =>
  (value) =>
    functions.reduce((result, fn) => fn(result), value);
```

---

# 45. Common Mistakes

- Using `var` in modern code.
- Mutating React state directly.
- Forgetting `return` inside arrow function block.
- Using `map` for side effects.
- Using index as key for dynamic React lists.
- Forgetting `await` or `return` in async code.
- Not handling rejected promises.
- Not checking `response.ok` after fetch.
- Assuming `setTimeout(..., 0)` runs immediately.
- Confusing `||` with `??`.
- Using `JSON.parse(JSON.stringify())` as universal deep clone.
- Sorting React state array directly.
- Missing cleanup for timers/listeners.
- Comparing objects by value using `===`.
- Trusting user input inside `innerHTML`.

Example object comparison:

```js
{} === {}; // false
[] === []; // false
```

Because objects compare by reference.

---

# 46. Quick Revision Cheat Sheet

```text
const = no reassignment
let = reassignment needed
var = avoid
primitive = copied by value
object = copied by reference
closure = function remembers outer variables
hoisting = declarations processed before execution
TDZ = let/const unavailable before declaration
this = depends on call-site, except arrow functions
prototype = inheritance lookup chain
=== = strict equality
?? = default only for null/undefined
?. = safe nested access
map = transform array
filter = keep matching items
find = first matching item
reduce = array to one value
forEach = side effects only
slice = non-mutating copy
splice = mutating remove/add
toSorted = non-mutating sort
Promise.all = fail fast
Promise.allSettled = wait for all
await = pauses async function, not JS thread
microtask = promise callback
task = timer/event callback
debounce = after quiet period
throttle = max once per interval
localStorage = persistent string storage
structuredClone = deep clone supported values
```

---

# 47. Final Interview Story

```text
I write modern JavaScript using const by default, let when reassignment is required, and avoid var. I understand closures, lexical scope, the event loop, promises, async/await, prototypes, and immutability. In React projects, I avoid mutating state, use stable keys, handle stale closures with functional updates, clean up async effects, and use modern array copy methods like toSorted when available.

For async code, I choose Promise.all for parallel fail-fast work, Promise.allSettled when partial failures are acceptable, and AbortController for cancelling fetches. For performance, I avoid blocking the main thread, debounce expensive input handlers, throttle scroll handlers, and measure before optimizing. I also keep track of ECMAScript updates but check runtime and browser support before using newest APIs in production.
```

---

# 48. References

- ECMAScript 2025 specification: https://tc39.es/ecma262/2025/multipage/
- Latest ECMAScript draft: https://tc39.es/ecma262/
- TC39 process: https://tc39.es/process-document/
- TC39 proposals: https://github.com/tc39/proposals
- TC39 finished proposals: https://github.com/tc39/proposals/blob/main/finished-proposals.md
- MDN JavaScript guide: https://developer.mozilla.org/en-US/docs/Web/JavaScript
