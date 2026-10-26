"use strict";
const $ = id => document.getElementById(id);
let page = 0, editing = null, pages = 0;
const token = document.querySelector('meta[name="csrf-token"]').content;
const csrfHeader = document.querySelector('meta[name="csrf-header"]').content;
function message(text, error = false) {
  $("message").textContent = text;
  $("message").classList.toggle("error", error);
  $("message").hidden = !text;
}
async function api(url, options = {}) {
  const response = await fetch(url, {...options, headers: {
    "Content-Type": "application/json", [csrfHeader]: token, ...options.headers
  }});
  if (response.status === 401) { location.assign("/login"); throw new Error("Please sign in again."); }
  if (!response.ok) {
    const detail = await response.json().catch(() => ({}));
    throw new Error(detail.detail || `Request failed (${response.status}). Please refresh and try again.`);
  }
  return response.status === 204 ? null : response.json();
}
function resetEditor() {
  editing = null; $("task-form").reset(); $("editor-title").textContent = "Add a task";
  $("save").textContent = "Add task"; $("cancel").hidden = true;
}
function button(text, className, action) {
  const node = document.createElement("button"); node.type = "button";
  node.textContent = text; node.className = className;
  node.addEventListener("click", async () => {
    node.disabled = true;
    try { await action(); } catch (error) { message(error.message, true); }
    finally { node.disabled = false; }
  });
  return node;
}
function renderTask(task) {
  const card = document.createElement("article"); card.className = "task-card" + (task.completed ? " done" : "");
  const toggle = document.createElement("input"); toggle.type = "checkbox";
  toggle.className = "task-check"; toggle.checked = task.completed;
  toggle.setAttribute("aria-label", `Mark ${task.title} ${task.completed ? "incomplete" : "complete"}`);
  toggle.addEventListener("change", async () => {
    toggle.disabled = true;
    try {
      await api(`/api/tasks/${task.id}`, {method:"PUT", body:JSON.stringify({...task, completed:toggle.checked})});
      if (editing === task.id) resetEditor();
      await load();
    } catch (error) { toggle.checked = task.completed; message(error.message, true); }
    finally { toggle.disabled = false; }
  });
  const content = document.createElement("div"); content.className = "task-content";
  const title = document.createElement("h3"); title.textContent = task.title;
  const notes = document.createElement("p"); notes.textContent = task.description;
  const actions = document.createElement("div"); actions.className = "task-actions";
  actions.append(button("Edit", "", () => {
    editing = task.id; $("title").value = task.title; $("description").value = task.description;
    $("completed").checked = task.completed; $("editor-title").textContent = "Edit task";
    $("save").textContent = "Save changes"; $("cancel").hidden = false; $("title").focus();
  }), button("Delete", "delete", async () => {
    if (!confirm(`Delete "${task.title}"?`)) return;
    await api(`/api/tasks/${task.id}`, {method:"DELETE"});
    if (editing === task.id) resetEditor();
    await load(); message("Task deleted.");
  }));
  content.append(title, notes, actions); card.append(toggle, content); return card;
}
async function load() {
  const data = await api(`/api/tasks?page=${page}&size=20`);
  pages = data.pages;
  if (page > 0 && page >= pages) { page = Math.max(0, pages - 1); return load(); }
  $("tasks").replaceChildren(...data.items.map(renderTask));
  $("total").textContent = data.total;
  $("progress").textContent = `${data.items.filter(t => t.completed).length} completed on this page`;
  $("empty").hidden = data.items.length !== 0;
  $("previous").disabled = page === 0; $("next").disabled = page + 1 >= pages;
  $("page-label").textContent = `Page ${page + 1} of ${Math.max(1, pages)}`;
}
$("task-form").addEventListener("submit", async event => {
  event.preventDefault(); $("save").disabled = true;
  const id = editing;
  try {
    await api(id === null ? "/api/tasks" : `/api/tasks/${id}`, {
      method:id === null ? "POST" : "PUT",
      body:JSON.stringify({title:$("title").value, description:$("description").value, completed:$("completed").checked})
    });
    resetEditor(); if (id === null) page = 0;
    await load(); message(id === null ? "Task added." : "Changes saved.");
  } catch (error) { message(error.message, true); }
  finally { $("save").disabled = false; }
});
$("cancel").addEventListener("click", resetEditor);
async function navigate(delta) {
  const previous = page;
  $("previous").disabled = true; $("next").disabled = true;
  page += delta;
  try { await load(); } catch (error) { page = previous; message(error.message, true);
    $("previous").disabled = page === 0; $("next").disabled = page + 1 >= pages;
  }
}
$("previous").addEventListener("click", () => navigate(-1));
$("next").addEventListener("click", () => navigate(1));
load().catch(error => message(error.message, true));
