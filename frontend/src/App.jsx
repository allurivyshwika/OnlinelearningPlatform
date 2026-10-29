import { useEffect, useMemo, useState } from 'react';

const API = (import.meta.env.VITE_API_URL || 'http://localhost:8080/api').replace(/\/$/, '');
const demoCourses = [
  { id: 1, title: 'React JS Fundamentals', description: 'Build modern interfaces with reusable components, props, state, and hooks.', instructor: 'Ananya Rao', category: 'Web Development', durationHours: 8 },
  { id: 2, title: 'Java and Spring MVC', description: 'Create Java web applications and REST APIs with a structured MVC design.', instructor: 'Rahul Kumar', category: 'Backend Development', durationHours: 10 },
  { id: 3, title: 'MySQL Essentials', description: 'Learn relational databases, SQL queries, joins, and data modelling.', instructor: 'Meera Shah', category: 'Database', durationHours: 6 }
];

export default function App() {
  const [courses, setCourses] = useState(demoCourses);
  const [user, setUser] = useState(() => {
    try { return JSON.parse(localStorage.getItem('learnspace-user') || 'null'); } catch { return null; }
  });
  const [enrolled, setEnrolled] = useState([]);
  const [search, setSearch] = useState('');
  const [notice, setNotice] = useState('');
  const [modal, setModal] = useState('');
  const [form, setForm] = useState({ name: '', email: '', password: '' });
  const [apiStatus, setApiStatus] = useState('Connecting to backend…');

  useEffect(() => {
    fetch(`${API}/courses`).then(r => { if (!r.ok) throw new Error(); return r.json(); })
      .then(data => { setCourses(data); setApiStatus('Connected to backend'); })
      .catch(() => setApiStatus('Demo mode — start the Spring backend to enable database features'));
  }, []);

  useEffect(() => {
    if (!user) { setEnrolled([]); return; }
    fetch(`${API}/enrollments/user/${user.id}`).then(r => r.ok ? r.json() : []).then(setEnrolled).catch(() => setEnrolled([]));
  }, [user]);

  const filtered = useMemo(() => courses.filter(c => `${c.title} ${c.category} ${c.instructor}`.toLowerCase().includes(search.toLowerCase())), [courses, search]);
  const updateForm = e => setForm({ ...form, [e.target.name]: e.target.value });

  async function submitAuth(e) {
    e.preventDefault();
    try {
      const endpoint = modal === 'register' ? 'register' : 'login';
      const response = await fetch(`${API}/auth/${endpoint}`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form)
      });
      const data = await response.json();
      if (!response.ok) throw new Error(data.message || 'Unable to continue.');
      if (modal === 'register') {
        setNotice('Account created. Please log in with your details.');
        setModal('login'); setForm({ name: '', email: form.email, password: '' }); return;
      }
      setUser(data); localStorage.setItem('learnspace-user', JSON.stringify(data));
      setModal(''); setForm({ name: '', email: '', password: '' }); setNotice(`Welcome, ${data.name}!`);
    } catch (err) {
      setNotice(`${err.message} If the backend is not running, start it and try again.`);
    }
  }

  async function enroll(courseId) {
    if (!user) { setNotice('Please log in before enrolling in a course.'); setModal('login'); return; }
    try {
      const response = await fetch(`${API}/enrollments`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId: user.id, courseId })
      });
      const data = await response.json();
      if (!response.ok) throw new Error(data.message || 'Enrollment failed.');
      const refreshed = await fetch(`${API}/enrollments/user/${user.id}`);
      if (refreshed.ok) setEnrolled(await refreshed.json());
      setNotice('You are enrolled! Open My Learning to track your progress.');
    } catch (err) { setNotice(`${err.message} Check that the backend and database are running.`); }
  }

  function logout() {
    localStorage.removeItem('learnspace-user'); setUser(null); setNotice('You have logged out.');
  }

  return <div className="app-shell">
    <header className="topbar">
      <a className="brand" href="#"><span className="brand-mark">L</span> LearnSpace</a>
      <nav><a href="#courses">Explore courses</a><a href="#how">How it works</a>{user && <a href="#learning">My Learning</a>}</nav>
      <div className="auth-actions">{user ? <><span className="user-chip">{user.name}</span><button className="btn btn-quiet" onClick={logout}>Log out</button></> : <><button className="btn btn-quiet" onClick={() => {setModal('login'); setNotice('');}}>Log in</button><button className="btn btn-primary" onClick={() => {setModal('register'); setNotice('');}}>Get started</button></>}</div>
    </header>

    <main>
      <section className="hero">
        <div className="hero-copy"><div className="eyebrow"><span className="eyebrow-dot"></span> LEARN SOMETHING THAT MOVES YOU</div>
          <h1>Your next skill starts <span>right here.</span></h1>
          <p className="hero-text">Build real-world skills with practical courses, learn at your own pace, and keep your progress in one place.</p>
          <div className="hero-actions"><a className="btn btn-primary btn-large" href="#courses">Explore courses <span>↗</span></a><a className="text-link" href="#how">See how it works <span>→</span></a></div>
          <div className="hero-proof"><div className="avatar-stack"><span>A</span><span>M</span><span>R</span></div><div><strong>Small steps. Real progress.</strong><p>Learn a little every day.</p></div></div>
        </div>
        <div className="hero-art" aria-label="Learning illustration"><div className="orbit orbit-one"></div><div className="orbit orbit-two"></div><div className="float-card float-top"><span className="mini-icon purple">✦</span><div><strong>Keep growing</strong><small>One lesson at a time</small></div></div><div className="learning-window"><div className="window-dots"><i></i><i></i><i></i><span>YOUR LEARNING SPACE</span></div><div className="window-content"><div className="play-circle">▶</div><div className="art-lines"><b></b><b></b><b></b></div><div className="progress-label"><span>Weekly progress</span><strong>68%</strong></div><div className="progress-track"><i></i></div><div className="art-bottom"><span>✦ Stay curious</span><span>↗ Keep going</span></div></div></div><div className="float-card float-bottom"><span className="mini-icon green">✓</span><div><strong>Progress saved</strong><small>Your effort adds up</small></div></div></div>
      </section>

      <section className="stats-strip"><div><strong>{courses.length.toString().padStart(2, '0')}+</strong><span>Learning courses</span></div><div><strong>Learn</strong><span>At your own pace</span></div><div><strong>Track</strong><span>Your progress</span></div><div><strong>Grow</strong><span>Every single day</span></div></section>

      <section className="courses-section" id="courses"><div className="section-heading"><div><div className="eyebrow">YOUR LEARNING JOURNEY</div><h2>Explore popular courses</h2><p>Practical knowledge for your next big step.</p></div><label className="search-box"><span>⌕</span><input value={search} onChange={e => setSearch(e.target.value)} placeholder="Search courses..." /></label></div>
        {notice && <div className="notice" role="status"><span>{notice}</span><button onClick={() => setNotice('')} aria-label="Dismiss">×</button></div>}
        <div className="course-grid">{filtered.map((course, index) => <article className="course-card" key={course.id}><div className={`course-art art-${index % 3}`}><span className="course-category">{course.category}</span><div className="course-symbol">{index % 3 === 0 ? '〈/〉' : index % 3 === 1 ? '⌘' : '▤'}</div><span className="course-number">0{index + 1}</span></div><div className="course-info"><div className="course-meta"><span>◷ {course.durationHours} hours</span><span>Beginner friendly</span></div><h3>{course.title}</h3><p>{course.description}</p><div className="course-footer"><div className="instructor"><span className="instructor-avatar">{(course.instructor || 'L').charAt(0)}</span><span>{course.instructor}</span></div><button className="enroll-btn" onClick={() => enroll(course.id)}>Enroll <span>↗</span></button></div></div></article>)}</div>
        {filtered.length === 0 && <div className="empty-state">No courses match that search. Try another keyword.</div>}
      </section>

      {user && <section className="learning-section" id="learning"><div className="section-heading"><div><div className="eyebrow">YOUR DASHBOARD</div><h2>My learning</h2><p>Pick up where you left off.</p></div><span className="dashboard-count">{enrolled.length} enrolled</span></div>{enrolled.length ? <div className="enrolled-grid">{enrolled.map(item => <article className="enrolled-card" key={item.enrollmentId}><div><span className="course-category">{item.course.category}</span><h3>{item.course.title}</h3><p>{item.course.instructor}</p></div><div className="progress-label"><span>Course progress</span><strong>{item.progressPercent}%</strong></div><div className="progress-track"><i style={{width: `${item.progressPercent}%`}}></i></div><button className="btn btn-quiet" onClick={async () => { const next = Math.min(100, item.progressPercent + 25); try { const r = await fetch(`${API}/enrollments/${item.enrollmentId}/progress`, {method:'PUT', headers:{'Content-Type':'application/json'}, body:JSON.stringify({progressPercent:next})}); if(!r.ok) throw new Error(); setEnrolled(old => old.map(x => x.enrollmentId === item.enrollmentId ? {...x, progressPercent:next} : x)); } catch { setNotice('Could not update progress. Check the backend connection.'); } }}>Mark next lesson complete</button></article>)}</div> : <div className="empty-state">You haven't enrolled in a course yet. Explore the courses above to get started.</div>}</section>}

      <section className="how-section" id="how"><div className="eyebrow">SIMPLE BY DESIGN</div><h2>A better way to learn</h2><div className="steps-grid"><article><span className="step-no">01</span><div className="step-icon">⌕</div><h3>Find your course</h3><p>Explore topics that match your interests and goals.</p></article><article><span className="step-no">02</span><div className="step-icon">↗</div><h3>Learn your way</h3><p>Enroll and work through learning at your own pace.</p></article><article><span className="step-no">03</span><div className="step-icon">✓</div><h3>See your progress</h3><p>Track completed lessons and keep building momentum.</p></article></div></section>
    </main>
    <footer><a className="brand" href="#"><span className="brand-mark">L</span> LearnSpace</a><span>Designed for curious minds.</span><span className="api-status"><i className={apiStatus.startsWith('Connected') ? 'status-live' : ''}></i>{apiStatus}</span><span>© {new Date().getFullYear()} LearnSpace</span></footer>

    {modal && <div className="modal-backdrop" onClick={e => {if(e.target === e.currentTarget) setModal('');}}><div className="auth-modal"><button className="modal-close" onClick={() => setModal('')} aria-label="Close">×</button><div className="brand"><span className="brand-mark">L</span> LearnSpace</div><div className="eyebrow">{modal === 'register' ? 'START YOUR JOURNEY' : 'WELCOME BACK'}</div><h2>{modal === 'register' ? 'Create your account' : 'Log in to learn'}</h2><p>{modal === 'register' ? 'A few details and you are ready to go.' : 'Your next lesson is waiting.'}</p><form onSubmit={submitAuth}>{modal === 'register' && <label>Full name<input name="name" value={form.name} onChange={updateForm} required placeholder="Your name" /></label>}<label>Email address<input name="email" type="email" value={form.email} onChange={updateForm} required placeholder="you@example.com" /></label><label>Password<input name="password" type="password" minLength="6" value={form.password} onChange={updateForm} required placeholder="At least 6 characters" /></label><button className="btn btn-primary btn-large" type="submit">{modal === 'register' ? 'Create account' : 'Log in'} <span>→</span></button></form><div className="switch-auth">{modal === 'register' ? 'Already have an account?' : "Don't have an account?"} <button onClick={() => {setModal(modal === 'register' ? 'login' : 'register'); setNotice('');}}>{modal === 'register' ? 'Log in' : 'Sign up'}</button></div></div></div>}
  </div>;
}
