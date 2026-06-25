# Explored React JS

Revision notes for a 5 years React.js developer interview. The goal is to revise concepts, explain implementation tradeoffs, and answer senior-level interview questions with examples.

Current version context: React docs currently target React 19.2. React 19 introduced Actions, `useActionState`, `useOptimistic`, `use`, better form handling, and Server Components support. React 19.2 added `<Activity />`, `useEffectEvent`, `cacheSignal`, React Performance Tracks, and React DOM server rendering improvements. React 18 introduced concurrent rendering foundations, automatic batching, transitions, and streaming SSR.

---

# 1. React In One Paragraph

React is a JavaScript library for building user interfaces using components. A component describes what UI should look like for a given state. React updates the UI by comparing previous and next render output through reconciliation, then committing the minimal DOM changes. Modern React is built around function components, hooks, one-way data flow, declarative rendering, and composition.

Interview answer:

```text
React lets us build UI as reusable components. Components render based on props and state. When state changes, React re-renders the component tree, compares the new virtual representation with the previous one, and updates the actual DOM efficiently. React is declarative, so we describe UI state instead of manually manipulating DOM nodes.
```

---

# 2. Core Concepts

## Component

A component is a reusable UI unit.

```jsx
function UserCard({ user }) {
  return (
    <article>
      <h2>{user.name}</h2>
      <p>{user.email}</p>
    </article>
  );
}
```

Rules:

- Component name starts with capital letter.
- Component returns JSX.
- Component should be pure during render.
- Do not mutate props.
- Keep component focused.

## JSX

JSX is syntax extension that looks like HTML but compiles to JavaScript.

```jsx
const title = "Orders";

function Header() {
  return <h1>{title}</h1>;
}
```

Important JSX rules:

- Return one parent element or fragment.
- Use `className`, not `class`.
- Use `htmlFor`, not `for`.
- JavaScript expressions go inside `{}`.
- Inline style is object: `style={{ color: "red" }}`.

## Props

Props are input from parent to child.

```jsx
function Button({ label, onClick, disabled = false }) {
  return (
    <button disabled={disabled} onClick={onClick}>
      {label}
    </button>
  );
}
```

Props are read-only. If child needs to change parent state, pass a callback.

```jsx
function Parent() {
  const [count, setCount] = useState(0);

  return <Counter value={count} onIncrement={() => setCount(count + 1)} />;
}
```

## State

State is data owned by a component.

```jsx
function Counter() {
  const [count, setCount] = useState(0);

  return (
    <button onClick={() => setCount(count + 1)}>
      Count: {count}
    </button>
  );
}
```

Use state for data that affects rendering.

Avoid state for:

- Values that can be derived from props/state.
- Constants.
- Temporary variables inside event handlers.
- Values that should not trigger render, use `useRef`.

## One-Way Data Flow

Data flows from parent to child through props. Events flow upward through callbacks.

```text
Parent state -> Child props
Child event  -> Parent callback -> Parent updates state
```

This makes UI predictable.

---

# 3. Rendering, Reconciliation, And Commit

React update has two main phases:

```text
Render phase -> Commit phase
```

## Render Phase

React calls components and calculates what UI should look like.

Important:

- Render should be pure.
- Do not call APIs, mutate DOM, or update state directly during render.
- React may pause, restart, or discard render work in concurrent rendering.

## Commit Phase

React applies changes to the DOM and runs effects.

```text
State update
  -> render component tree
  -> diff old/new output
  -> commit DOM changes
  -> run layout effects
  -> browser paints
  -> run passive effects
```

## Virtual DOM

Virtual DOM is a plain JS representation of UI. React uses it to compare previous and next UI and decide what DOM operations are needed.

Interview note:

```text
React is not fast because virtual DOM is always faster than DOM. React is effective because it lets us write declarative UI, batches updates, and minimizes/directs DOM work through reconciliation.
```

## Reconciliation

React compares old and new element trees.

Rules:

- Different element types cause subtree replacement.
- Same element type updates props.
- Keys help React match list items.

Bad key:

```jsx
items.map((item, index) => <Todo key={index} item={item} />);
```

Good key:

```jsx
items.map((item) => <Todo key={item.id} item={item} />);
```

Use index as key only for static lists that never reorder, insert, or delete.

---

# 4. React Version Timeline

## React 16.8

Major change:

- Hooks introduced.

Important hooks:

- `useState`
- `useEffect`
- `useContext`
- `useReducer`
- `useMemo`
- `useCallback`
- `useRef`
- `useLayoutEffect`
- `useImperativeHandle`
- `useDebugValue`

Interview point:

```text
Hooks let function components use state, lifecycle-like effects, context, refs, memoization, and reducers without class components.
```

## React 17

Major change:

- No big new developer API.
- Upgrade stepping stone.
- New JSX transform.
- Event delegation changed from document root to React root.

Interview point:

```text
React 17 was mainly a gradual upgrade release. It made it easier to run multiple React versions on one page and introduced the new JSX transform so importing React just for JSX became unnecessary in modern tooling.
```

## React 18

Major changes:

- Concurrent rendering foundation.
- `createRoot`.
- Automatic batching.
- Transitions.
- Suspense improvements.
- Streaming server-side rendering.
- New hooks: `useId`, `useTransition`, `useDeferredValue`, `useSyncExternalStore`, `useInsertionEffect`.

Example upgrade:

```jsx
import { createRoot } from "react-dom/client";

const root = createRoot(document.getElementById("root"));
root.render(<App />);
```

Automatic batching:

```jsx
function handleClick() {
  setCount((c) => c + 1);
  setFlag((f) => !f);
  // React batches these into one render.
}
```

## React 18.3

Purpose:

- Same behavior as React 18.2.
- Adds warnings to prepare for React 19 upgrade.

Interview point:

```text
Before moving a large app to React 19, upgrade to React 18.3 first to find deprecated API usage and migration warnings.
```

## React 19

Major changes:

- Actions for async updates.
- `useActionState`.
- `useOptimistic`.
- `use`.
- `useFormStatus` from `react-dom`.
- Better form actions.
- Ref as prop for function components.
- Document metadata support.
- Asset loading improvements.
- Custom Elements improvements.
- React Server Components support in stable ecosystem/framework integrations.

Example action:

```jsx
function UpdateName() {
  const [error, submitAction, isPending] = useActionState(
    async (previousState, formData) => {
      const name = formData.get("name");
      await updateName(name);
      return null;
    },
    null
  );

  return (
    <form action={submitAction}>
      <input name="name" />
      <button disabled={isPending}>Save</button>
      {error && <p>{error}</p>}
    </form>
  );
}
```

## React 19.2

Major additions:

- `<Activity />` for hiding/showing UI while preserving state.
- `useEffectEvent` for non-reactive logic inside effects.
- `cacheSignal` for server cache lifetime cancellation.
- React Performance Tracks in DevTools.
- Partial pre-rendering support in React DOM.
- SSR improvements such as batching Suspense boundaries.

Security note:

- React Server Components had critical vulnerabilities disclosed in late 2025.
- Fixed versions include `19.0.1`, `19.1.2`, and `19.2.1` or newer for affected server component packages.
- Client-only React apps without RSC/framework support were not affected by that specific RSC issue.

Interview point:

```text
React 19.2 is important because it improves real-world async, effect, performance, and server rendering workflows. For projects using Server Components, staying on patched versions is important.
```

---

# 5. Hooks Rules

## Rules Of Hooks

- Call hooks only at the top level.
- Do not call hooks inside loops, conditions, or nested functions.
- Call hooks only from React function components or custom hooks.

Bad:

```jsx
function Profile({ user }) {
  if (user) {
    const [name, setName] = useState(user.name);
  }
}
```

Good:

```jsx
function Profile({ user }) {
  const [name, setName] = useState(user?.name ?? "");

  if (!user) {
    return null;
  }

  return <h1>{name}</h1>;
}
```

Why:

```text
React identifies hook state by call order. Conditional hook calls break that order.
```

---

# 6. Important Hooks With Examples

## 6.1 `useState`

Use for local component state.

```jsx
function SearchBox() {
  const [query, setQuery] = useState("");

  return (
    <input
      value={query}
      onChange={(event) => setQuery(event.target.value)}
      placeholder="Search"
    />
  );
}
```

Functional update:

```jsx
setCount((previous) => previous + 1);
```

Use functional update when next state depends on previous state.

Lazy initial state:

```jsx
const [settings, setSettings] = useState(() => readSettingsFromLocalStorage());
```

This avoids running expensive initialization on every render.

Common mistake:

```jsx
setCount(count + 1);
setCount(count + 1);
```

This may increment once because both updates use same stale `count`.

Correct:

```jsx
setCount((count) => count + 1);
setCount((count) => count + 1);
```

## 6.2 `useEffect`

Use for synchronizing with external systems.

Examples:

- Fetching data.
- Subscriptions.
- Timers.
- DOM/browser APIs.
- Analytics.

```jsx
function UserPage({ userId }) {
  const [user, setUser] = useState(null);

  useEffect(() => {
    let ignore = false;

    async function loadUser() {
      const response = await fetch(`/api/users/${userId}`);
      const data = await response.json();

      if (!ignore) {
        setUser(data);
      }
    }

    loadUser();

    return () => {
      ignore = true;
    };
  }, [userId]);

  return user ? <h1>{user.name}</h1> : <p>Loading...</p>;
}
```

Dependency array:

```jsx
useEffect(() => {
  document.title = title;
}, [title]);
```

Cleanup:

```jsx
useEffect(() => {
  const id = setInterval(() => {
    console.log("tick");
  }, 1000);

  return () => clearInterval(id);
}, []);
```

Important:

- Empty dependency array means run after initial mount.
- No dependency array means run after every render.
- Dependencies should include all reactive values used inside effect.
- Do not use effects for simple derived state.

Bad derived state:

```jsx
const [fullName, setFullName] = useState("");

useEffect(() => {
  setFullName(firstName + " " + lastName);
}, [firstName, lastName]);
```

Good:

```jsx
const fullName = `${firstName} ${lastName}`;
```

## 6.3 `useLayoutEffect`

Runs synchronously after DOM mutations but before browser paint.

Use for:

- Measuring DOM.
- Positioning tooltip/popover before paint.
- Avoiding flicker for layout calculations.

```jsx
function Tooltip({ targetRef }) {
  const tooltipRef = useRef(null);
  const [position, setPosition] = useState({ top: 0, left: 0 });

  useLayoutEffect(() => {
    const rect = targetRef.current.getBoundingClientRect();
    setPosition({ top: rect.bottom + 8, left: rect.left });
  }, [targetRef]);

  return (
    <div ref={tooltipRef} style={{ position: "absolute", ...position }}>
      Tooltip
    </div>
  );
}
```

Interview point:

```text
useEffect runs after paint. useLayoutEffect runs before paint and can block painting. Use useLayoutEffect only when layout measurement must happen before the user sees the screen.
```

## 6.4 `useRef`

Stores mutable value that does not trigger re-render.

DOM ref:

```jsx
function FocusInput() {
  const inputRef = useRef(null);

  return (
    <>
      <input ref={inputRef} />
      <button onClick={() => inputRef.current.focus()}>Focus</button>
    </>
  );
}
```

Mutable value:

```jsx
function Timer() {
  const intervalRef = useRef(null);

  function start() {
    intervalRef.current = setInterval(() => {
      console.log("tick");
    }, 1000);
  }

  function stop() {
    clearInterval(intervalRef.current);
  }

  return (
    <>
      <button onClick={start}>Start</button>
      <button onClick={stop}>Stop</button>
    </>
  );
}
```

State vs ref:

| Need | Use |
|---|---|
| UI should update when value changes | `useState` |
| Store mutable value without re-render | `useRef` |
| Access DOM node | `useRef` |

## 6.5 `useMemo`

Memoizes calculated value.

```jsx
const filteredUsers = useMemo(() => {
  return users.filter((user) =>
    user.name.toLowerCase().includes(search.toLowerCase())
  );
}, [users, search]);
```

Use when:

- Calculation is expensive.
- Stable object/array reference is needed for memoized child.

Avoid when:

- Calculation is cheap.
- You are using it everywhere without measuring.

## 6.6 `useCallback`

Memoizes function reference.

```jsx
const handleDelete = useCallback((id) => {
  setItems((items) => items.filter((item) => item.id !== id));
}, []);
```

Use when:

- Passing callback to `React.memo` child.
- Callback is dependency of another hook.
- Stable reference is necessary.

Common misconception:

```text
useCallback does not prevent function creation completely. It returns a cached function reference when dependencies do not change.
```

## 6.7 `useContext`

Reads context value from nearest provider.

```jsx
const AuthContext = createContext(null);

function AuthProvider({ children }) {
  const [user, setUser] = useState(null);

  const value = useMemo(() => ({ user, setUser }), [user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

function UserMenu() {
  const { user } = useContext(AuthContext);
  return <span>{user?.name ?? "Guest"}</span>;
}
```

Context is good for:

- Theme.
- Auth user.
- Locale.
- Feature flags.
- App-level settings.

Context is not automatically a performance solution. When provider value changes, consumers re-render.

Tricks:

- Split contexts by update frequency.
- Memoize provider value.
- Put state and dispatch in separate contexts for reducers.

## 6.8 `useReducer`

Use for complex state transitions.

```jsx
const initialState = {
  items: [],
  loading: false,
  error: null,
};

function reducer(state, action) {
  switch (action.type) {
    case "loadStarted":
      return { ...state, loading: true, error: null };
    case "loadSuccess":
      return { ...state, loading: false, items: action.payload };
    case "loadFailed":
      return { ...state, loading: false, error: action.error };
    default:
      throw new Error(`Unknown action: ${action.type}`);
  }
}

function TodoList() {
  const [state, dispatch] = useReducer(reducer, initialState);

  return state.loading ? <p>Loading...</p> : <TodoItems items={state.items} />;
}
```

Use `useReducer` when:

- State has multiple related fields.
- Updates are event-like.
- You want predictable transitions.
- Logic is hard to manage with many `useState` calls.

## 6.9 `useImperativeHandle`

Customize methods exposed through ref.

```jsx
const TextInput = forwardRef(function TextInput(props, ref) {
  const inputRef = useRef(null);

  useImperativeHandle(ref, () => ({
    focus() {
      inputRef.current.focus();
    },
    clear() {
      inputRef.current.value = "";
    },
  }));

  return <input ref={inputRef} {...props} />;
});
```

Use rarely. Prefer declarative props first.

## 6.10 `useDebugValue`

Shows custom hook labels in React DevTools.

```jsx
function useOnlineStatus() {
  const [isOnline, setIsOnline] = useState(navigator.onLine);

  useDebugValue(isOnline ? "Online" : "Offline");

  return isOnline;
}
```

Useful in shared/custom hook libraries.

## 6.11 `useTransition`

Marks state updates as non-urgent.

```jsx
function ProductSearch({ products }) {
  const [query, setQuery] = useState("");
  const [filteredQuery, setFilteredQuery] = useState("");
  const [isPending, startTransition] = useTransition();

  function handleChange(event) {
    const value = event.target.value;
    setQuery(value);

    startTransition(() => {
      setFilteredQuery(value);
    });
  }

  const filtered = products.filter((product) =>
    product.name.toLowerCase().includes(filteredQuery.toLowerCase())
  );

  return (
    <>
      <input value={query} onChange={handleChange} />
      {isPending && <span>Updating...</span>}
      <ProductList products={filtered} />
    </>
  );
}
```

Use when:

- Input should stay responsive.
- Heavy UI update can be delayed.
- Navigation/list rendering is expensive.

## 6.12 `useDeferredValue`

Defers a value so urgent UI updates happen first.

```jsx
function SearchPage({ products }) {
  const [query, setQuery] = useState("");
  const deferredQuery = useDeferredValue(query);

  const filtered = useMemo(() => {
    return products.filter((product) =>
      product.name.toLowerCase().includes(deferredQuery.toLowerCase())
    );
  }, [products, deferredQuery]);

  return (
    <>
      <input value={query} onChange={(event) => setQuery(event.target.value)} />
      <ProductList products={filtered} />
    </>
  );
}
```

Difference from debounce:

- Debounce waits for time.
- Deferred value lets React schedule lower priority rendering.

## 6.13 `useId`

Generates stable unique IDs, useful for accessibility and SSR.

```jsx
function EmailField() {
  const id = useId();

  return (
    <>
      <label htmlFor={id}>Email</label>
      <input id={id} type="email" />
    </>
  );
}
```

Do not use `useId` for list keys. Use stable data IDs for keys.

## 6.14 `useSyncExternalStore`

Subscribes to external stores safely with concurrent rendering.

```jsx
function subscribe(callback) {
  window.addEventListener("online", callback);
  window.addEventListener("offline", callback);

  return () => {
    window.removeEventListener("online", callback);
    window.removeEventListener("offline", callback);
  };
}

function getSnapshot() {
  return navigator.onLine;
}

function useOnlineStatus() {
  return useSyncExternalStore(subscribe, getSnapshot);
}
```

Use for:

- External state stores.
- Browser APIs.
- Custom subscriptions.

## 6.15 `useInsertionEffect`

Runs before layout effects, mainly for CSS-in-JS libraries to inject styles.

```jsx
useInsertionEffect(() => {
  injectStyles(rule);
}, [rule]);
```

Most app developers rarely need this hook.

## 6.16 `useActionState`

React 19 hook for form/action state.

```jsx
function LoginForm() {
  const [state, loginAction, isPending] = useActionState(
    async (previousState, formData) => {
      const email = formData.get("email");
      const password = formData.get("password");

      if (!email || !password) {
        return { error: "Email and password are required" };
      }

      await login(email, password);
      return { error: null };
    },
    { error: null }
  );

  return (
    <form action={loginAction}>
      <input name="email" type="email" />
      <input name="password" type="password" />
      <button disabled={isPending}>Login</button>
      {state.error && <p>{state.error}</p>}
    </form>
  );
}
```

Use for:

- Forms.
- Async mutations.
- Pending state.
- Error/result state.

## 6.17 `useOptimistic`

React 19 hook for optimistic UI.

```jsx
function Comments({ initialComments }) {
  const [comments, setComments] = useState(initialComments);
  const [optimisticComments, addOptimisticComment] = useOptimistic(
    comments,
    (currentComments, newComment) => [
      ...currentComments,
      { id: "temp", text: newComment, sending: true },
    ]
  );

  async function submitComment(formData) {
    const text = formData.get("comment");
    addOptimisticComment(text);

    const saved = await saveComment(text);
    setComments((current) => [...current, saved]);
  }

  return (
    <>
      <form action={submitComment}>
        <input name="comment" />
        <button>Add</button>
      </form>

      <ul>
        {optimisticComments.map((comment) => (
          <li key={comment.id}>
            {comment.text} {comment.sending && "Sending..."}
          </li>
        ))}
      </ul>
    </>
  );
}
```

Use for:

- Likes.
- Comments.
- Follow/unfollow.
- Cart updates.

Need rollback/error UX for failures.

## 6.18 `use`

React 19 API to read resources like promises or context during render.

Conceptual example with Suspense:

```jsx
function UserDetails({ userPromise }) {
  const user = use(userPromise);

  return <h1>{user.name}</h1>;
}

function Page({ userPromise }) {
  return (
    <Suspense fallback={<p>Loading user...</p>}>
      <UserDetails userPromise={userPromise} />
    </Suspense>
  );
}
```

Important:

- `use` integrates with Suspense.
- Often used through frameworks that support React Server Components.
- Do not replace every client fetch with `use` without understanding framework support.

## 6.19 `useFormStatus`

React DOM hook for child components inside forms.

```jsx
import { useFormStatus } from "react-dom";

function SubmitButton() {
  const { pending } = useFormStatus();

  return <button disabled={pending}>Submit</button>;
}

function ProfileForm({ saveProfile }) {
  return (
    <form action={saveProfile}>
      <input name="name" />
      <SubmitButton />
    </form>
  );
}
```

Use when submit button is nested and needs form pending state.

## 6.20 `useEffectEvent`

React 19.2 hook for extracting non-reactive effect logic.

Problem:

```jsx
useEffect(() => {
  const connection = createConnection(roomId);

  connection.on("connected", () => {
    showNotification("Connected", theme);
  });

  connection.connect();
  return () => connection.disconnect();
}, [roomId, theme]);
```

This reconnects when `theme` changes, even though only notification should use latest theme.

Better:

```jsx
function ChatRoom({ roomId, theme }) {
  const onConnected = useEffectEvent(() => {
    showNotification("Connected", theme);
  });

  useEffect(() => {
    const connection = createConnection(roomId);
    connection.on("connected", onConnected);
    connection.connect();

    return () => connection.disconnect();
  }, [roomId]);
}
```

Use for:

- Reading latest props/state inside effects without making effect reactive to them.
- Event-like callbacks triggered from subscriptions.

Do not use it to hide real dependencies.

---

# 7. Custom Hooks

Custom hooks extract reusable stateful logic.

## Example: Fetch Hook

```jsx
function useFetch(url) {
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const controller = new AbortController();

    async function load() {
      setLoading(true);
      setError(null);

      try {
        const response = await fetch(url, { signal: controller.signal });
        if (!response.ok) {
          throw new Error(`Request failed: ${response.status}`);
        }
        setData(await response.json());
      } catch (error) {
        if (error.name !== "AbortError") {
          setError(error);
        }
      } finally {
        setLoading(false);
      }
    }

    load();

    return () => controller.abort();
  }, [url]);

  return { data, error, loading };
}
```

Usage:

```jsx
function UsersPage() {
  const { data, error, loading } = useFetch("/api/users");

  if (loading) return <p>Loading...</p>;
  if (error) return <p>{error.message}</p>;

  return <UserList users={data} />;
}
```

Production note:

```text
For serious server state, prefer TanStack Query, SWR, Relay, Apollo, or framework data loaders instead of writing every cache/retry/refetch behavior manually.
```

## Example: Debounce Hook

```jsx
function useDebouncedValue(value, delay) {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    const id = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => clearTimeout(id);
  }, [value, delay]);

  return debouncedValue;
}
```

Usage:

```jsx
function Search() {
  const [query, setQuery] = useState("");
  const debouncedQuery = useDebouncedValue(query, 300);

  useEffect(() => {
    if (debouncedQuery) {
      searchProducts(debouncedQuery);
    }
  }, [debouncedQuery]);

  return <input value={query} onChange={(e) => setQuery(e.target.value)} />;
}
```

---

# 8. Component Design Patterns

## Container And Presentational Components

Container handles data and logic.

```jsx
function UsersContainer() {
  const { data: users, loading } = useFetch("/api/users");

  if (loading) return <p>Loading...</p>;

  return <UsersList users={users} />;
}
```

Presentational component handles UI.

```jsx
function UsersList({ users }) {
  return (
    <ul>
      {users.map((user) => (
        <li key={user.id}>{user.name}</li>
      ))}
    </ul>
  );
}
```

## Compound Components

Useful for flexible component APIs.

```jsx
const TabsContext = createContext(null);

function Tabs({ children }) {
  const [activeTab, setActiveTab] = useState(0);
  return (
    <TabsContext.Provider value={{ activeTab, setActiveTab }}>
      {children}
    </TabsContext.Provider>
  );
}

function TabList({ children }) {
  return <div role="tablist">{children}</div>;
}

function Tab({ index, children }) {
  const { activeTab, setActiveTab } = useContext(TabsContext);

  return (
    <button
      role="tab"
      aria-selected={activeTab === index}
      onClick={() => setActiveTab(index)}
    >
      {children}
    </button>
  );
}

function TabPanel({ index, children }) {
  const { activeTab } = useContext(TabsContext);
  return activeTab === index ? <div role="tabpanel">{children}</div> : null;
}

Tabs.List = TabList;
Tabs.Tab = Tab;
Tabs.Panel = TabPanel;
```

Usage:

```jsx
<Tabs>
  <Tabs.List>
    <Tabs.Tab index={0}>Profile</Tabs.Tab>
    <Tabs.Tab index={1}>Security</Tabs.Tab>
  </Tabs.List>
  <Tabs.Panel index={0}>Profile content</Tabs.Panel>
  <Tabs.Panel index={1}>Security content</Tabs.Panel>
</Tabs>
```

## Render Props

Pass rendering function as prop.

```jsx
function MouseTracker({ render }) {
  const [position, setPosition] = useState({ x: 0, y: 0 });

  return (
    <div onMouseMove={(event) => setPosition({ x: event.clientX, y: event.clientY })}>
      {render(position)}
    </div>
  );
}
```

Usage:

```jsx
<MouseTracker render={({ x, y }) => <p>{x}, {y}</p>} />
```

Modern replacement is usually custom hooks.

## Higher-Order Component

Function that takes component and returns enhanced component.

```jsx
function withAuth(Component) {
  return function AuthenticatedComponent(props) {
    const user = useAuth();

    if (!user) {
      return <Navigate to="/login" />;
    }

    return <Component {...props} user={user} />;
  };
}
```

Use carefully because wrapper nesting can become hard to debug. Hooks often replace HOCs.

---

# 9. State Management

## Local State

Use local state when data belongs to one component or small subtree.

```jsx
const [isOpen, setIsOpen] = useState(false);
```

## Lift State Up

Move state to nearest common parent.

```jsx
function Parent() {
  const [selectedId, setSelectedId] = useState(null);

  return (
    <>
      <Sidebar selectedId={selectedId} onSelect={setSelectedId} />
      <Details selectedId={selectedId} />
    </>
  );
}
```

## Context State

Use for cross-cutting state.

Good:

- Theme.
- Auth.
- Locale.
- Feature flags.

Avoid using one huge context for the whole app because every update can re-render many consumers.

## Server State

Server state is data owned by backend.

Challenges:

- Loading.
- Caching.
- Refetching.
- Background updates.
- Deduplication.
- Pagination.
- Optimistic updates.

Use libraries:

- TanStack Query.
- SWR.
- Apollo/Relay for GraphQL.
- Framework loaders in Next.js/Remix/React Router.

## Global Client State

Use only when needed.

Options:

- Redux Toolkit.
- Zustand.
- Jotai.
- Recoil.
- Context + reducer for small apps.

Redux Toolkit example:

```jsx
const cartSlice = createSlice({
  name: "cart",
  initialState: [],
  reducers: {
    itemAdded(state, action) {
      state.push(action.payload);
    },
    itemRemoved(state, action) {
      return state.filter((item) => item.id !== action.payload);
    },
  },
});
```

Interview answer:

```text
I do not put all state in Redux by default. I separate local UI state, server state, URL state, and global client state. Server state usually belongs in TanStack Query/SWR/framework loaders. Global client state is for data many unrelated parts of the app need to read or modify.
```

## URL State

Use URL for state that should be shareable/bookmarkable.

Examples:

- Search query.
- Filters.
- Sort.
- Page number.
- Selected tab.

```jsx
const [searchParams, setSearchParams] = useSearchParams();
const page = Number(searchParams.get("page") ?? 1);
```

---

# 10. Forms

## Controlled Component

React state controls input value.

```jsx
function LoginForm() {
  const [email, setEmail] = useState("");

  return (
    <input
      value={email}
      onChange={(event) => setEmail(event.target.value)}
    />
  );
}
```

Pros:

- Easy validation.
- State is always available.
- Controlled UI.

Cons:

- Can re-render often for large forms.

## Uncontrolled Component

DOM keeps value; read using ref or `FormData`.

```jsx
function LoginForm() {
  function handleSubmit(event) {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);
    login(formData.get("email"), formData.get("password"));
  }

  return (
    <form onSubmit={handleSubmit}>
      <input name="email" />
      <input name="password" type="password" />
      <button>Login</button>
    </form>
  );
}
```

For large forms:

- React Hook Form is popular because it minimizes re-renders.
- Zod/Yup can validate schemas.

## Validation Example With Zod Style

```jsx
function validateUser(values) {
  const errors = {};

  if (!values.email.includes("@")) {
    errors.email = "Invalid email";
  }

  if (values.password.length < 8) {
    errors.password = "Password must be at least 8 characters";
  }

  return errors;
}
```

## React 19 Form Action Style

```jsx
function Signup() {
  const [state, submit, isPending] = useActionState(
    async (previous, formData) => {
      const result = await createUser({
        email: formData.get("email"),
        password: formData.get("password"),
      });

      return result.ok ? { error: null } : { error: result.message };
    },
    { error: null }
  );

  return (
    <form action={submit}>
      <input name="email" />
      <input name="password" type="password" />
      <button disabled={isPending}>Create account</button>
      {state.error && <p>{state.error}</p>}
    </form>
  );
}
```

---

# 11. Data Fetching

## Fetch In Effect

Basic client-side fetching:

```jsx
function Products() {
  const [products, setProducts] = useState([]);
  const [status, setStatus] = useState("idle");

  useEffect(() => {
    const controller = new AbortController();

    async function load() {
      setStatus("loading");
      const response = await fetch("/api/products", {
        signal: controller.signal,
      });
      const data = await response.json();
      setProducts(data);
      setStatus("success");
    }

    load().catch((error) => {
      if (error.name !== "AbortError") {
        setStatus("error");
      }
    });

    return () => controller.abort();
  }, []);

  if (status === "loading") return <p>Loading...</p>;
  if (status === "error") return <p>Failed</p>;

  return <ProductList products={products} />;
}
```

Limitations:

- No cache.
- No deduplication.
- Manual retries.
- Waterfall risk.
- Manual loading/error states.

Better for production:

- Use framework data APIs.
- Use TanStack Query/SWR.
- Use GraphQL client if GraphQL.
- Use Suspense-enabled frameworks where appropriate.

## Race Condition Fix

When request depends on parameter:

```jsx
useEffect(() => {
  const controller = new AbortController();

  fetch(`/api/users/${userId}`, { signal: controller.signal })
    .then((response) => response.json())
    .then(setUser)
    .catch((error) => {
      if (error.name !== "AbortError") {
        setError(error);
      }
    });

  return () => controller.abort();
}, [userId]);
```

## Loading State Pattern

Avoid many booleans:

```jsx
const [status, setStatus] = useState("idle");
```

Possible values:

```text
idle -> loading -> success
idle -> loading -> error
```

For complex flows, use reducer or state machine.

---

# 12. Performance Optimization

## First Rule

Measure before optimizing.

Tools:

- React DevTools Profiler.
- Browser Performance tab.
- Lighthouse.
- Web Vitals.
- React Performance Tracks in React 19.2.

## Common Causes Of Slow React Apps

- Rendering huge lists without virtualization.
- Expensive calculations during render.
- Large context updates.
- Unstable object/function props passed to memoized children.
- Too much global state causing broad re-renders.
- Heavy third-party bundles.
- Images not optimized.
- Fetch waterfalls.

## `React.memo`

Memoizes component render when props are same.

```jsx
const ProductRow = memo(function ProductRow({ product, onSelect }) {
  return (
    <button onClick={() => onSelect(product.id)}>
      {product.name}
    </button>
  );
});
```

Use with stable props:

```jsx
const handleSelect = useCallback((id) => {
  setSelectedId(id);
}, []);
```

Do not wrap every component with `memo`. It adds comparison cost.

## List Virtualization

For thousands of rows, render only visible rows.

Libraries:

- `react-window`.
- `@tanstack/react-virtual`.

Concept:

```text
10,000 records in data
Only 30 rows mounted in DOM
```

## Code Splitting

```jsx
const AdminPage = lazy(() => import("./AdminPage"));

function App() {
  return (
    <Suspense fallback={<p>Loading...</p>}>
      <AdminPage />
    </Suspense>
  );
}
```

Use for:

- Routes.
- Heavy charts.
- Admin sections.
- Rich text editors.

## Avoid Derived State

Bad:

```jsx
const [filtered, setFiltered] = useState([]);

useEffect(() => {
  setFiltered(items.filter((item) => item.active));
}, [items]);
```

Good:

```jsx
const filtered = useMemo(
  () => items.filter((item) => item.active),
  [items]
);
```

If cheap:

```jsx
const filtered = items.filter((item) => item.active);
```

## Avoid Inline Objects For Memoized Children

Bad:

```jsx
<Chart options={{ color: "blue" }} />
```

Better:

```jsx
const options = useMemo(() => ({ color: "blue" }), []);
<Chart options={options} />;
```

Only matters when child relies on reference equality.

## Context Performance Trick

Bad:

```jsx
<AppContext.Provider value={{ user, theme, cart, setCart }}>
```

Every change can re-render all consumers.

Better:

```jsx
<AuthProvider>
  <ThemeProvider>
    <CartProvider>{children}</CartProvider>
  </ThemeProvider>
</AuthProvider>
```

Split by domain/update frequency.

---

# 13. Error Handling

## Error Boundary

Class component error boundary:

```jsx
class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error, info) {
    reportError(error, info);
  }

  render() {
    if (this.state.hasError) {
      return <p>Something went wrong.</p>;
    }

    return this.props.children;
  }
}
```

Usage:

```jsx
<ErrorBoundary>
  <Dashboard />
</ErrorBoundary>
```

Important:

- Error boundaries catch render/lifecycle errors below them.
- They do not catch event handler errors.
- They do not catch async promise errors unless thrown during render/Suspense flow.

Event handler error:

```jsx
function handleClick() {
  try {
    riskyAction();
  } catch (error) {
    reportError(error);
  }
}
```

---

# 14. Suspense

Suspense lets components wait for something before rendering.

```jsx
<Suspense fallback={<Spinner />}>
  <UserProfile />
</Suspense>
```

Common use:

- Lazy-loaded components.
- Framework data loading.
- React 19 `use` with promises.
- Server rendering streams.

Lazy component:

```jsx
const SettingsPage = lazy(() => import("./SettingsPage"));
```

Nested Suspense:

```jsx
<Suspense fallback={<PageSkeleton />}>
  <ProfileHeader />
  <Suspense fallback={<PostsSkeleton />}>
    <Posts />
  </Suspense>
</Suspense>
```

Interview point:

```text
Suspense is not only a loading spinner. It is a coordination mechanism for rendering async UI, code splitting, and streaming server rendering.
```

---

# 15. Routing

React itself does not provide routing. Common option: React Router.

## Basic React Router

```jsx
import { createBrowserRouter, RouterProvider } from "react-router-dom";

const router = createBrowserRouter([
  {
    path: "/",
    element: <RootLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: "users/:userId", element: <UserPage /> },
    ],
  },
]);

function App() {
  return <RouterProvider router={router} />;
}
```

## Route Params

```jsx
function UserPage() {
  const { userId } = useParams();
  return <h1>User {userId}</h1>;
}
```

## Query Params

```jsx
function ProductsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const query = searchParams.get("q") ?? "";

  function updateQuery(value) {
    setSearchParams({ q: value });
  }
}
```

## Protected Route

```jsx
function ProtectedRoute({ children }) {
  const user = useAuth();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return children;
}
```

---

# 16. Styling Approaches

Options:

- Plain CSS.
- CSS Modules.
- Sass.
- Tailwind CSS.
- CSS-in-JS.
- Component libraries like MUI, Ant Design, Chakra UI, Mantine.

## CSS Modules

```jsx
import styles from "./Button.module.css";

function Button({ children }) {
  return <button className={styles.primary}>{children}</button>;
}
```

Pros:

- Locally scoped class names.
- Simple mental model.
- Build-time CSS.

## Conditional Classes

```jsx
function Button({ active }) {
  return (
    <button className={active ? "button button-active" : "button"}>
      Save
    </button>
  );
}
```

With helper:

```jsx
className={clsx("button", active && "button-active")}
```

## Styling Interview Note

```text
I choose styling based on team and project needs. For design systems, CSS Modules or CSS-in-JS can help encapsulation. For fast product UI, Tailwind can be productive. For enterprise apps, consistency and accessibility are more important than the styling library itself.
```

---

# 17. Accessibility

Important for senior React developers.

Checklist:

- Use semantic HTML.
- Buttons for actions, links for navigation.
- Labels for inputs.
- Keyboard navigation.
- Focus management for modals.
- ARIA only when semantic HTML is not enough.
- Color contrast.
- Error messages connected to fields.

Example:

```jsx
function EmailInput({ error }) {
  const id = useId();
  const errorId = `${id}-error`;

  return (
    <div>
      <label htmlFor={id}>Email</label>
      <input
        id={id}
        type="email"
        aria-invalid={Boolean(error)}
        aria-describedby={error ? errorId : undefined}
      />
      {error && <p id={errorId}>{error}</p>}
    </div>
  );
}
```

Modal focus:

- Focus first interactive element when modal opens.
- Trap focus inside modal.
- Return focus to trigger when modal closes.
- Close on Escape if appropriate.

---

# 18. Testing

## Testing Pyramid

- Unit tests for pure functions/reducers.
- Component tests for UI behavior.
- Integration tests for flows.
- E2E tests for critical journeys.

## React Testing Library

Test user behavior, not implementation details.

```jsx
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

test("increments counter", async () => {
  const user = userEvent.setup();
  render(<Counter />);

  await user.click(screen.getByRole("button", { name: /count/i }));

  expect(screen.getByText(/count: 1/i)).toBeInTheDocument();
});
```

Good queries:

- `getByRole`
- `getByLabelText`
- `getByText`
- `findBy...` for async UI

Avoid:

- Testing internal state directly.
- Overusing test IDs.
- Snapshot testing everything.

## Mock API

Use MSW for realistic API mocking.

```jsx
http.get("/api/users", () => {
  return HttpResponse.json([{ id: 1, name: "Anjali" }]);
});
```

---

# 19. Build Tools And Project Setup

## Common Modern Setup

- Vite for SPA.
- Next.js for full-stack/server-rendered React.
- Remix/React Router framework for data routing.
- TypeScript for large teams.
- ESLint + Prettier.
- Vitest/Jest + Testing Library.
- Playwright/Cypress for E2E.

## Vite Setup

```bash
npm create vite@latest my-app -- --template react-ts
cd my-app
npm install
npm run dev
```

## Recommended `src` Structure

Feature-based:

```text
src/
  app/
    router.jsx
    providers.jsx
  features/
    auth/
      components/
      hooks/
      api/
      types.ts
    orders/
      components/
      hooks/
      api/
  shared/
    components/
    hooks/
    utils/
```

Avoid placing all components in one giant `components/` folder for large apps.

---

# 20. TypeScript With React

## Props Type

```tsx
type ButtonProps = {
  children: React.ReactNode;
  onClick: () => void;
  disabled?: boolean;
};

function Button({ children, onClick, disabled = false }: ButtonProps) {
  return (
    <button disabled={disabled} onClick={onClick}>
      {children}
    </button>
  );
}
```

## Event Types

```tsx
function SearchInput() {
  function handleChange(event: React.ChangeEvent<HTMLInputElement>) {
    console.log(event.target.value);
  }

  return <input onChange={handleChange} />;
}
```

## Ref Type

```tsx
const inputRef = useRef<HTMLInputElement | null>(null);
```

## State Type

```tsx
type User = {
  id: string;
  name: string;
};

const [user, setUser] = useState<User | null>(null);
```

Interview note:

```text
I avoid React.FC by default unless the team standard uses it. Explicit props typing is usually clearer and avoids implicit children confusion.
```

---

# 21. Implementation Tricks

## Trick 1: Reset Component State With Key

```jsx
function UserDetailsWrapper({ userId }) {
  return <UserDetails key={userId} userId={userId} />;
}
```

When `userId` changes, React remounts `UserDetails` and resets internal state.

## Trick 2: Preserve State By Keeping Component Position Stable

State is tied to component position in the tree.

```jsx
{isAdmin ? <Dashboard /> : <Dashboard />}
```

Same component at same position preserves state.

Different key or different component type resets state.

## Trick 3: Use Functional Updates To Avoid Stale State

```jsx
setItems((items) => [...items, newItem]);
```

Better than:

```jsx
setItems([...items, newItem]);
```

when update may run after previous queued updates.

## Trick 4: Store Previous Value

```jsx
function usePrevious(value) {
  const ref = useRef();

  useEffect(() => {
    ref.current = value;
  }, [value]);

  return ref.current;
}
```

## Trick 5: Click Outside Hook

```jsx
function useClickOutside(ref, onOutsideClick) {
  useEffect(() => {
    function handleMouseDown(event) {
      if (ref.current && !ref.current.contains(event.target)) {
        onOutsideClick();
      }
    }

    document.addEventListener("mousedown", handleMouseDown);
    return () => document.removeEventListener("mousedown", handleMouseDown);
  }, [ref, onOutsideClick]);
}
```

Use with stable callback if needed.

## Trick 6: Abort Fetch On Unmount

```jsx
useEffect(() => {
  const controller = new AbortController();

  fetch(url, { signal: controller.signal });

  return () => controller.abort();
}, [url]);
```

## Trick 7: Dynamic Import Heavy Component

```jsx
const RichEditor = lazy(() => import("./RichEditor"));
```

## Trick 8: Use URL For Filters

```jsx
setSearchParams({ status: "active", page: "1" });
```

This makes filter state shareable and restorable.

## Trick 9: Avoid Prop Drilling With Composition

Instead of:

```jsx
<Layout user={user} notifications={notifications} />
```

Use:

```jsx
<Layout
  header={<Header user={user} />}
  sidebar={<Sidebar notifications={notifications} />}
>
  <Dashboard />
</Layout>
```

## Trick 10: Use Event Delegation For Large Lists

Instead of one handler per row if needed:

```jsx
function List({ items, onSelect }) {
  function handleClick(event) {
    const id = event.target.closest("[data-id]")?.dataset.id;
    if (id) onSelect(id);
  }

  return (
    <ul onClick={handleClick}>
      {items.map((item) => (
        <li key={item.id} data-id={item.id}>
          {item.name}
        </li>
      ))}
    </ul>
  );
}
```

---

# 22. Common Mistakes

- Mutating state directly.
- Missing effect dependencies.
- Using effects for derived state.
- Using index as key in dynamic lists.
- Putting everything in global state.
- Creating one huge context.
- Overusing `useMemo` and `useCallback`.
- Fetching data in many nested components and creating waterfalls.
- Not handling loading/error/empty states.
- Not aborting stale requests.
- Not testing user behavior.
- Ignoring accessibility.
- Not measuring performance before optimizing.
- Using React as if it were jQuery by manually manipulating DOM.

State mutation bad:

```jsx
items.push(newItem);
setItems(items);
```

Correct:

```jsx
setItems((items) => [...items, newItem]);
```

Object update:

```jsx
setUser((user) => ({
  ...user,
  name: "New Name",
}));
```

Nested update:

```jsx
setOrder((order) => ({
  ...order,
  customer: {
    ...order.customer,
    name: "Anjali",
  },
}));
```

For complex nested state, consider normalizing data or using Immer.

---

# 23. React Interview Questions And Answers

## What Is React?

React is a component-based JavaScript library for building UI. It uses declarative rendering, props, state, hooks, and reconciliation to update the UI when data changes.

## React Library Or Framework?

React is a UI library. It does not include routing, data fetching, build system, or full application conventions by itself. Frameworks like Next.js provide those.

## What Is JSX?

JSX is syntax that lets us write UI-like markup inside JavaScript. It compiles to JavaScript function calls that create React elements.

## What Are Props?

Props are read-only inputs passed from parent to child.

## What Is State?

State is component-owned data that causes re-render when updated.

## Props vs State

| Props | State |
|---|---|
| Passed from parent | Managed inside component |
| Read-only | Updated with setter/reducer |
| Used for configuration/data input | Used for interactive changing data |

## Controlled vs Uncontrolled Components

Controlled input value is managed by React state. Uncontrolled input value is managed by DOM and read using ref/FormData.

## What Is Reconciliation?

Reconciliation is React's process of comparing previous and next render output to decide what UI changes need to be committed.

## Why Keys Are Important?

Keys help React identify list items between renders. Stable keys prevent incorrect state reuse and unnecessary DOM changes.

## What Is Virtual DOM?

Virtual DOM is a JS representation of UI. React uses it for diffing and reconciliation before updating real DOM.

## What Are Hooks?

Hooks are functions that let function components use React features such as state, effects, context, refs, reducers, and memoization.

## Why Hooks Cannot Be Conditional?

React tracks hooks by call order. Conditional calls change order between renders and break React's hook state mapping.

## `useEffect` vs `useLayoutEffect`

`useEffect` runs after paint. `useLayoutEffect` runs after DOM update but before paint. Use layout effect only for DOM measurement or visual correction before paint.

## `useMemo` vs `useCallback`

`useMemo` memoizes a value. `useCallback` memoizes a function reference.

```jsx
const value = useMemo(() => calculate(a, b), [a, b]);
const callback = useCallback(() => doSomething(id), [id]);
```

## `useRef` vs `useState`

`useState` triggers re-render when changed. `useRef` stores mutable value without re-render.

## Context vs Redux

Context passes values through component tree. Redux is a predictable global state management library with devtools, middleware, and structured updates. Context can replace Redux for simple global values, but it is not the same as a complete state management solution.

## What Is Prop Drilling?

Passing props through many intermediate components that do not use them. Solve with composition, context, or state colocating.

## What Is Lifting State Up?

Moving shared state to nearest common parent so multiple children can use and update it.

## What Is React.memo?

`React.memo` prevents re-render of a component if props are shallowly equal. Useful for expensive components with stable props.

## What Is Suspense?

Suspense lets part of UI wait for async work and show fallback. It is used for lazy loading, framework data loading, server rendering, and React 19 `use`.

## What Is Error Boundary?

Error boundary catches rendering errors in child tree and shows fallback UI. Traditionally implemented as class component.

## What Is Hydration?

Hydration is when React attaches event handlers and client behavior to HTML already rendered by server.

## CSR vs SSR vs SSG

| Rendering | Meaning |
|---|---|
| CSR | Browser renders app using JS |
| SSR | Server renders HTML per request |
| SSG | HTML generated at build time |
| ISR | Static pages regenerated after deployment in frameworks like Next.js |

## What Is Concurrent Rendering?

Concurrent rendering lets React interrupt, pause, resume, or abandon rendering work to keep UI responsive. It is enabled through features like transitions and concurrent root.

## What Is Automatic Batching?

React batches multiple state updates into one render for better performance. React 18 expanded batching beyond React event handlers.

## What Is Transition?

A transition marks an update as non-urgent so urgent updates like typing stay responsive.

## What Is Server Component?

Server Components render on the server and do not ship their component JavaScript to the client. They can access server resources directly but cannot use client-only hooks like `useState` or browser APIs.

## Client Component vs Server Component

Client component:

- Runs in browser.
- Can use state/effects/events.
- Can access browser APIs.

Server component:

- Runs on server.
- Can fetch data directly.
- Cannot use state/effects/browser event handlers.
- Reduces client bundle size.

## What Is Hydration Error?

Hydration error happens when server-rendered HTML does not match client render output. Common causes include random values, dates, browser-only data, or conditional rendering that differs between server and client.

Fix:

- Use stable initial render.
- Move browser-only logic to `useEffect`.
- Avoid `Math.random()` or `Date.now()` during SSR render.

---

# 24. Senior-Level Design Answers

## How Do You Structure A React App?

```text
For small apps, simple folders are fine. For larger apps, I prefer feature-based structure. Each feature owns its components, hooks, API calls, tests, and types. Shared reusable UI goes into shared/components. App-level providers and routing stay in app/. This keeps feature ownership clear and reduces cross-folder coupling.
```

## How Do You Decide State Location?

```text
I keep state as close as possible to where it is used. If siblings need it, I lift it to the nearest common parent. If many unrelated components need it, I consider context or a state library. If it comes from the backend, I treat it as server state and use a data fetching/cache solution.
```

## How Do You Optimize React Performance?

```text
First I profile. Then I look for unnecessary re-renders, heavy calculations, large lists, expensive children, context updates, and bundle size. Solutions may include memoization, splitting context, virtualization, code splitting, moving state down, using transitions, and optimizing images/network requests.
```

## How Do You Handle API Calls?

```text
For simple screens, useEffect with AbortController can work. For production server state, I prefer TanStack Query/SWR/framework loaders because they handle cache, refetch, retries, deduplication, loading and error states better than manual effects.
```

## How Do You Handle Forms?

```text
For small forms, controlled components are simple. For large forms, I prefer React Hook Form because it reduces re-renders and handles validation ergonomically. In React 19 capable projects, form actions with useActionState and useFormStatus can simplify async form flows.
```

## How Do You Review A React PR?

Checklist:

- Is component responsibility clear?
- Is state colocated?
- Are keys stable?
- Are effects necessary and dependencies correct?
- Are async requests cancelled/handled?
- Are loading/error/empty states handled?
- Are components accessible?
- Are expensive renders measured or optimized?
- Are tests covering user behavior?
- Is API/error handling consistent?
- Is bundle impact acceptable?

---

# 25. Quick Revision Cheat Sheet

```text
React = declarative component UI library
Component = reusable UI unit
Props = parent to child input
State = data that triggers render
JSX = UI syntax compiled to JS
Render phase = calculate UI
Commit phase = apply DOM changes
Reconciliation = compare old/new tree
Key = stable identity in lists
Hook = function for React features
useState = local state
useEffect = sync with external system
useLayoutEffect = before paint layout work
useRef = mutable value/DOM ref without render
useMemo = memoized value
useCallback = memoized function
useContext = read context
useReducer = complex state transitions
useTransition = non-urgent updates
useDeferredValue = defer low-priority value
useId = stable IDs for accessibility/SSR
useSyncExternalStore = external store subscription
useActionState = React 19 action/form state
useOptimistic = React 19 optimistic UI
useFormStatus = form pending status
useEffectEvent = React 19.2 non-reactive effect logic
Suspense = async UI coordination
Error Boundary = render error fallback
Hydration = attach React to server HTML
CSR = client render
SSR = server per request render
SSG = build-time render
RSC = server-only React component model
```

---

# 26. Version Enhancement Summary

| Version | Important Enhancements |
|---|---|
| 16.8 | Hooks |
| 17 | Gradual upgrade, new JSX transform, root event delegation |
| 18 | Concurrent rendering foundation, automatic batching, transitions, streaming SSR, new concurrent hooks |
| 18.3 | React 19 migration warnings |
| 19 | Actions, `useActionState`, `useOptimistic`, `use`, form improvements, ref as prop, metadata/assets/custom elements improvements |
| 19.2 | `<Activity />`, `useEffectEvent`, `cacheSignal`, Performance Tracks, Partial Pre-rendering, SSR improvements |

---

# 27. Final Interview Story

```text
In a React project, I design UI as small reusable components and keep state as close as possible to where it is used. I separate local UI state, URL state, server state, and global client state. I use hooks for state/effects/context and custom hooks for reusable behavior. I avoid unnecessary effects, use stable keys, handle loading/error/empty states, and focus on accessibility.

For performance, I profile first, then optimize specific bottlenecks using memoization, virtualization, code splitting, context splitting, and transitions. For data fetching, I prefer framework loaders or TanStack Query/SWR instead of manually building cache/retry logic everywhere. For modern React, I understand React 18 concurrent features and React 19 improvements like Actions, useActionState, useOptimistic, use, and React 19.2's useEffectEvent and Activity.
```

---

# 28. References

- React docs: https://react.dev/
- React versions: https://react.dev/versions
- React 18 release: https://react.dev/blog/2022/03/29/react-v18
- React 19 release: https://react.dev/blog/2024/12/05/react-19
- React 19 upgrade guide: https://react.dev/blog/2024/04/25/react-19-upgrade-guide
- React 19.2 release: https://react.dev/blog/2025/10/01/react-19-2
- React Server Components security advisory: https://react.dev/blog/2025/12/03/critical-security-vulnerability-in-react-server-components
- React Compiler v1.0: https://react.dev/blog/2025/10/07/react-compiler-1
